package com.zylitics.front.api;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.*;
import com.zylitics.front.services.EmailService;
import com.zylitics.front.services.SendTemplatedEmail;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Optional;

@SuppressWarnings("unused")
@RestController
@RequestMapping("${app-short-version}/user")
public class UserController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
  
  private final APICoreProperties apiCoreProperties;
  
  private final EmailVerificationProvider emailVerificationProvider;
  
  private final PasswordResetProvider passwordResetProvider;
  
  private final EmailChangeProvider emailChangeProvider;
  
  private final UserProvider userProvider;
  
  private final OrganizationProvider organizationProvider;
  
  private final FirebaseAuth firebaseAuth;
  
  private final EmailService emailService;
  
  public UserController(APICoreProperties apiCoreProperties,
                        EmailVerificationProvider emailVerificationProvider,
                        PasswordResetProvider passwordResetProvider,
                        EmailChangeProvider emailChangeProvider,
                        UserProvider userProvider,
                        OrganizationProvider organizationProvider,
                        FirebaseAuth firebaseAuth,
                        EmailService emailService) {
    this.apiCoreProperties = apiCoreProperties;
    this.emailVerificationProvider = emailVerificationProvider;
    this.passwordResetProvider = passwordResetProvider;
    this.emailChangeProvider = emailChangeProvider;
    this.userProvider = userProvider;
    this.organizationProvider = organizationProvider;
    this.firebaseAuth = firebaseAuth;
    this.emailService = emailService;
  }
  
  @SuppressWarnings("unused")
  /*
  * Client must put restrictions on password length which should be atleast 6 characters
  * */
  @PostMapping
  public ResponseEntity<?> newUser(
      @RequestBody @Validated NewUserRequest newUserRequest) {
    Optional<EmailVerification> emailVerificationOptional =
        emailVerificationProvider.getEmailVerification(newUserRequest.getEmailVerificationId());
    if (!emailVerificationOptional.isPresent()) {
      throw new IllegalArgumentException("Invalid emailVerificationId passed to newUser: " +
          newUserRequest.getEmailVerificationId());
    }
    EmailVerification emailVerification = emailVerificationOptional.get();
    String shotBucketSessionStorage =
        Common.getShotBucketPerOffset(newUserRequest.getUtcOffsetInMinutes());
    String organizationName = newUserRequest.getOrganizationName();
    NewUser newUser;
    if (emailVerification.getOrganizationId() != null) {
      newUser = new NewUserInOrganization(newUserRequest.getFirstName(),
          newUserRequest.getLastName(), emailVerification.getEmail(), newUserRequest.getTimezone(),
          emailVerification.getRole(), shotBucketSessionStorage,
          newUserRequest.getEmailVerificationId(), emailVerification.getOrganizationId());
    } else {
      PlanName planName;
      if (emailVerification.getEmailVerificationUserType() ==
          EmailVerificationUserType.BETA_INVITEE) {
        planName = PlanName.BETA_TEST;
      } else {
        planName = PlanName.FREE_TRIAL;
      }
      if (Strings.isNullOrEmpty(organizationName)) {
        organizationName = newUserRequest.getFirstName() + "'s" + " Organization";
      }
      newUser = new NewUserNewOrganization(newUserRequest.getFirstName(),
          newUserRequest.getLastName(), emailVerification.getEmail(), newUserRequest.getTimezone(),
          Role.ADMIN, shotBucketSessionStorage, newUserRequest.getEmailVerificationId(), planName,
          organizationName);
    }
    // put this new user
    User user = userProvider.newUser(newUser);
    int userId = user.getId();
    // put this new user in firebase using our own userId
    CreateRequest createRequest = new CreateRequest()
        .setUid(Integer.toString(userId))
        .setEmail(emailVerification.getEmail())
        .setPassword(newUserRequest.getPassword())
        .setDisplayName(newUserRequest.getFirstName() + " " + newUserRequest.getLastName())
        .setEmailVerified(true);
    try {
      firebaseAuth.createUser(createRequest);
    } catch (FirebaseAuthException f) {
      LOG.error("Priority: Couldn't create user in firebase, userId: " + userId, f);
      return sendError(HttpStatus.INTERNAL_SERVER_ERROR, "There was an error signing you up." +
          " Your details have been saved and there is no" +
          " need to try signing up again. We will fix the issue, sign you up and notify on your" +
          " email once you account is ready to log in. Please allow us few hours time. You can" +
          " also contact us if you've questions.");
    }
    // send new user welcome email
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    EmailInfo emailInfo = new EmailInfo()
        .setFrom(emailProps.getNoReplyEmailSender())
        .setTo(emailVerification.getEmail());
    String templateId = emailVerification.getEmailVerificationUserType() ==
        EmailVerificationUserType.BETA_INVITEE
        ? emailProps.getEmailBetaWelcomeTmpId()
        : emailProps.getEmailWelcomeTmpId();
    SendTemplatedEmail sendTemplatedEmail = new SendTemplatedEmail(emailInfo, templateId, null,
        emailProps.getNotificationEmailGroupId(), null);
    boolean result = emailService.send(sendTemplatedEmail);
    if (!result) {
      LOG.error("Priority: Couldn't send a welcome email to userId: " + userId);
    }
    if (organizationName == null) {
      // User is NewUserInOrganization, get it from db
      organizationName = organizationProvider.getOrganization(user.getOrganizationId())
          .orElseThrow(RuntimeException::new).getName();
    }
    return ResponseEntity.ok(new NewUserResponse(shotBucketSessionStorage, user.getOrganizationId(),
        organizationName));
  }
  
  @SuppressWarnings("unused")
  @PatchMapping("/{passwordResetId}/resetPassword")
  public ResponseEntity<?> resetPassword(
      @RequestBody @Validated ResetPasswordRequest resetPasswordRequest,
      @PathVariable @Min(1) long passwordResetId) {
    Optional<PasswordReset> passwordResetOptional =
        passwordResetProvider.getPasswordReset(passwordResetId);
    if (!passwordResetOptional.isPresent()) {
      throw new IllegalArgumentException("Invalid passwordResetId passed to resetPassword: " +
          passwordResetId);
    }
    PasswordReset passwordReset = passwordResetOptional.get();
    try {
      firebaseAuth.updateUser(new UserRecord.UpdateRequest(
          Integer.toString(passwordReset.getUserId()))
          .setPassword(resetPasswordRequest.getPassword()));
    } catch (FirebaseAuthException f) {
      LOG.error("Priority: Couldn't reset user password in firebase, userId: " +
          passwordReset.getUserId(), f);
      return sendError(HttpStatus.INTERNAL_SERVER_ERROR, "There was an error resetting your" +
          " password. We've been notified and this should be fixed very soon. Please try" +
          " resetting again in a few hours or contact us if you see a similar problem.");
    }
    return ResponseEntity.ok().build();
  }
  
  @SuppressWarnings("unused")
  @PatchMapping("/{emailChangeId}/changeEmail")
  public ResponseEntity<?> changeEmail(
      @PathVariable @Min(1) long emailChangeId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    Optional<EmailChange> emailChangeOptional =
        emailChangeProvider.getEmailChange(emailChangeId);
    if (!emailChangeOptional.isPresent()) {
      throw new IllegalArgumentException("Invalid emailChangeId passed to changeEmail: " +
          emailChangeId);
    }
    EmailChange emailChange = emailChangeOptional.get();
    try {
      firebaseAuth.updateUser(new UserRecord.UpdateRequest(Integer.toString(userId))
          .setEmail(emailChange.getNewEmail()));
    } catch (FirebaseAuthException f) {
      LOG.error("Priority: Couldn't change user email in firebase, userId: " +
          userId, f);
      return sendError(HttpStatus.INTERNAL_SERVER_ERROR, "There was an error changing your" +
          " email. We've been notified and this should be fixed very soon. Please try" +
          " changing again in a few hours or contact us if you see a similar problem.");
    }
    // change in user table too, don't throw error if it fails and log
    try {
      userProvider.updateEmail(userId, emailChange.getNewEmail());
    } catch (Throwable t) {
      LOG.error("Priority: There was an error updating email in db for userId: " + userId, t);
    }
    return ResponseEntity.ok().build();
  }
  
  @Validated
  private static class ResetPasswordRequest {
  
    @NotBlank
    @Size(min = 6)
    private String password;
  
    @SuppressWarnings("unused")
    public String getPassword() {
      return password;
    }
  
    public ResetPasswordRequest setPassword(String password) {
      this.password = password;
      return this;
    }
  }
}
