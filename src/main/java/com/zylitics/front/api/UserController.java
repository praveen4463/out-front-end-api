package com.zylitics.front.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.exception.UnauthorizedException;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.*;
import com.zylitics.front.services.EmailService;
import com.zylitics.front.services.SendTemplatedEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unused")
@RestController
@RequestMapping("${app-short-version}/users")
public class UserController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
  
  private final APICoreProperties apiCoreProperties;
  
  private final EmailVerificationProvider emailVerificationProvider;
  
  private final UserProvider userProvider;
  
  private final FirebaseAuth firebaseAuth;
  
  private final EmailService emailService;
  
  public UserController(APICoreProperties apiCoreProperties,
                        EmailVerificationProvider emailVerificationProvider,
                        UserProvider userProvider,
                        FirebaseAuth firebaseAuth,
                        EmailService emailService) {
    this.apiCoreProperties = apiCoreProperties;
    this.emailVerificationProvider = emailVerificationProvider;
    this.userProvider = userProvider;
    this.firebaseAuth = firebaseAuth;
    this.emailService = emailService;
  }
  
  // All possible user types are:
  // identity provider user, email-pwd user, team user email-pwd, team user identity provider
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<?> newUser(
      @RequestBody @Validated NewUserRequest newUserRequest,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    assertAnonymousUser(userInfo);
    
    // check whether email is in system
    if (userProvider.userWithEmailExist(newUserRequest.getEmail())) {
      return sendError(HttpStatus.UNPROCESSABLE_ENTITY, "A user with this email already exists",
          EmailApiErrorCause.EMAIL_ALREADY_EXIST);
    }
  
    // if we've a password, check it's length. We may not have a password if signing using providers
    String password = newUserRequest.getPassword();
    if (!Strings.isNullOrEmpty(password)) {
      Preconditions.checkArgument(password.length() >= 6, "Password requirement not met");
    } else {
      // if there is no password, validate that a provider is used.
      Preconditions.checkArgument(newUserRequest.isUsingLoginProvider(), "Password can't be empty");
    }
    
    // get all the things needed to create new user depending on type of user
    long emailVerificationId;
    Role role;
    String newEmailVerificationCode = null;
    @Nullable Integer organizationId = null;
    // if this invocation doesn't have an email verification (regular signup), create a new one
    if (newUserRequest.getEmailVerificationId() == null) {
      role = Role.ADMIN;
      // when a login provider is used, just create email verification record and mark is used means
      // verified.
      newEmailVerificationCode = UUID.randomUUID().toString();
      emailVerificationId = emailVerificationProvider.newEmailVerification(
          new NewEmailVerification(newUserRequest.getEmail(),
              newEmailVerificationCode,
              EmailVerificationUserType.NORMAL,
              role,
              newUserRequest.isUsingLoginProvider()));
    } else {
      // email verification is done upfront in team member invite only
      emailVerificationId = newUserRequest.getEmailVerificationId();
      Optional<EmailVerification> emailVerificationOptional =
          emailVerificationProvider.getEmailVerification(emailVerificationId);
      if (!emailVerificationOptional.isPresent()) {
        throw new IllegalArgumentException("Invalid emailVerificationId passed to newUser: " +
            newUserRequest.getEmailVerificationId());
      }
      EmailVerification emailVerification = emailVerificationOptional.get();
      role = emailVerification.getRole();
      // will be available in team invitec
      organizationId = emailVerification.getOrganizationId();
    }
    
    String shotBucketSessionStorage =
        Common.getShotBucketPerOffset(newUserRequest.getUtcOffsetInMinutes(), apiCoreProperties);
    NewUser newUser;
    
    if (organizationId != null) {
      newUser = new NewUserInOrganization(newUserRequest.getFirstName(),
          newUserRequest.getLastName(), newUserRequest.getEmail(), newUserRequest.getTimezone(),
          role, shotBucketSessionStorage, emailVerificationId, organizationId);
    } else {
      String organizationName = newUserRequest.getOrganizationName();
      if (Strings.isNullOrEmpty(organizationName)) {
        organizationName = newUserRequest.getFirstName() + "'s" + " Organization";
      }
      Preconditions.checkArgument(newUserRequest.getPlanName() != null, "Plan can't be null");
      newUser = new NewUserNewOrganization(newUserRequest.getFirstName(),
          newUserRequest.getLastName(), newUserRequest.getEmail(), newUserRequest.getTimezone(),
          role, shotBucketSessionStorage, emailVerificationId, newUserRequest.getPlanName(),
          organizationName);
    }
    
    // put this new user
    User user = userProvider.newUser(newUser);
    int userId = user.getId();
    String firebaseUid = Integer.toString(userId);
    String customToken = null;
    // send email verification after signup only for users that created using email/pwd
    boolean sendEmailVerification = newUserRequest.getEmailVerificationId() == null
        && !newUserRequest.isUsingLoginProvider();
    // put this new user in firebase using our own userId
    CreateRequest createRequest = new CreateRequest()
        .setUid(firebaseUid)
        .setEmail(newUserRequest.getEmail())
        .setDisplayName(
            Common.getUserDisplayName(newUserRequest.getFirstName(), newUserRequest.getLastName()))
        .setEmailVerified(!sendEmailVerification);
    if (!Strings.isNullOrEmpty(password)) {
      createRequest.setPassword(password);
    }
    try {
      firebaseAuth.createUser(createRequest);
      // if a login provider was used, create a custom token to login user
      if (newUserRequest.isUsingLoginProvider()) {
        customToken = firebaseAuth.createCustomToken(firebaseUid);
      }
    } catch (FirebaseAuthException f) {
      LOG.error("Priority: Couldn't create user in firebase, userId: " + userId, f);
      return sendError(HttpStatus.INTERNAL_SERVER_ERROR, "There was an error signing you up." +
          " Your details have been saved and there is no" +
          " need to try signing up again. We will fix the issue, sign you up and notify on your" +
          " email once you account is ready to log in. Please allow us few hours time. You can" +
          " also contact us if you've questions.");
    }
    
    // send new user emails asynchronously
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    EmailInfo emailInfo = new EmailInfo()
        .setFromName(emailProps.getExternalEmailSenderName())
        .setFrom(emailProps.getSupportEmail())
        .setTo(newUserRequest.getEmail());
    // send verification email if needed
    if (sendEmailVerification) {
      Objects.requireNonNull(newEmailVerificationCode);
      String ctaLink = String.format("%s/%s",
          apiCoreProperties.getFrontEndBaseUrl() + emailProps.getVerifyEmailPage(),
          newEmailVerificationCode);
      Map<String, Object> templateData = ImmutableMap.of(emailProps.getCtaLinkTag(), ctaLink);
      SendTemplatedEmail sendTemplatedEmail = new SendTemplatedEmail(emailInfo,
          emailProps.getEmailVerifyTmpId(),
          templateData);
      emailService.sendAsync(sendTemplatedEmail, null,
          (v) -> LOG.error("Priority: Couldn't send an email verification to userId: " + userId));
    }
    // send welcome email
    SendTemplatedEmail sendTemplatedEmail = new SendTemplatedEmail(emailInfo,
        emailProps.getEmailWelcomeTmpId());
    emailService.sendAsync(sendTemplatedEmail, null,
        (v) -> LOG.error("Priority: Couldn't send a welcome email to userId: " + userId));
    
    return ResponseEntity.ok(new NewUserResponse().setUser(user).setCustomToken(customToken));
  }
  
  @GetMapping("/current")
  public ResponseEntity<User> getUser(
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    User user = userProvider.getUser(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));
    return ResponseEntity.ok(user);
  }
  
  @GetMapping("/current/getUserPlan")
  public ResponseEntity<UsersPlan> getUserPlan(
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    UsersPlan userPlan = (userProvider.getUser(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"))).getUsersPlan();
    Preconditions.checkArgument(userPlan != null);
    return ResponseEntity.ok(userPlan);
  }
  
  @PatchMapping("/current/updateUserProfile")
  public ResponseEntity<Void> updateUserProfile(
      @RequestBody UserUpdatableProfile userUpdatableProfile,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    userProvider.updateProfile(userId, userUpdatableProfile);
    String fn = userUpdatableProfile.getFirstName();
    String ln = userUpdatableProfile.getLastName();
    if (!Strings.isNullOrEmpty(fn) || !Strings.isNullOrEmpty(ln)) {
      try {
        // for updating in firebase whenever fn or ln is updated, api should get both as we're updating
        // full name, if this is not happening, log error, don't throw as we're not showing names from
        // firebase anywhere
        if (!Strings.isNullOrEmpty(fn) && !Strings.isNullOrEmpty(ln)) {
          LOG.error("Didn't update name in firebase as both first and last name are not given");
        } else {
          firebaseAuth.updateUser(new UserRecord.UpdateRequest(Integer.toString(userId))
              .setDisplayName(Common.getUserDisplayName(fn, ln)));
        }
      } catch (FirebaseAuthException f) {
        LOG.error("Couldn't change user name in firebase, userId: " +
            userId, f);
      }
    }
    return ResponseEntity.ok().build();
  }
  
  static class NewUserResponse {
    
    private User user;
    
    @Nullable
    private String customToken;
  
    public User getUser() {
      return user;
    }
  
    public NewUserResponse setUser(User user) {
      this.user = user;
      return this;
    }
  
    @Nullable
    public String getCustomToken() {
      return customToken;
    }
  
    public NewUserResponse setCustomToken(@Nullable String customToken) {
      this.customToken = customToken;
      return this;
    }
  }
}
