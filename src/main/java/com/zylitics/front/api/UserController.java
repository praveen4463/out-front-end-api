package com.zylitics.front.api;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
import com.zylitics.front.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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
  
  @SuppressWarnings("unused")
  /*
  * Client must put restrictions on password length which should be atleast 6 characters
  * */
  @PostMapping
  public ResponseEntity<?> newUser(
      @RequestBody @Validated NewUserRequest newUserRequest,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    assertAnonymousUser(userInfo);
    Optional<EmailVerification> emailVerificationOptional =
        emailVerificationProvider.getEmailVerification(newUserRequest.getEmailVerificationId());
    if (!emailVerificationOptional.isPresent()) {
      throw new IllegalArgumentException("Invalid emailVerificationId passed to newUser: " +
          newUserRequest.getEmailVerificationId());
    }
    EmailVerification emailVerification = emailVerificationOptional.get();
    // check whether email is in system, checked again here so that if some user sends
    // themselves multiple signup email, completes one of them and later click on others, we reject
    // them with proper error.
    if (userProvider.userWithEmailExist(emailVerification.getEmail())) {
      return sendError(HttpStatus.UNPROCESSABLE_ENTITY, "A user with this email already exists",
          EmailApiErrorCause.EMAIL_ALREADY_EXIST);
    }
    String password = newUserRequest.getPassword().trim();
    Preconditions.checkArgument(password.length() >= 6, "Password requirement not met");
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
        .setPassword(password)
        .setDisplayName(
            Common.getUserDisplayName(newUserRequest.getFirstName(), newUserRequest.getLastName()))
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
    // send new user welcome email asynchronously
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
    emailService.sendAsync(sendTemplatedEmail, null,
        (v) -> LOG.error("Priority: Couldn't send a welcome email to userId: " + userId));
    return ResponseEntity.ok(user);
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
  public ResponseEntity<UsersPlanResponse> getUserPlan(
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    UsersPlan userPlan = (userProvider.getUser(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"))).getUsersPlan();
    Preconditions.checkArgument(userPlan != null);
    UsersPlanResponse usersPlanResponse = new UsersPlanResponse()
        .setPlanType(userPlan.getPlanType())
        .setPlanName(userPlan.getPlanName())
        .setDisplayName(userPlan.getDisplayName())
        .setConsumedMinutes(userPlan.getConsumedMinutes())
        .setTotalParallel(userPlan.getTotalParallel())
        .setTotalMinutes(userPlan.getTotalMinutes())
        .setBillingCycleStart(DateTimeUtil.utcTimeToEpochSecs(userPlan.getBillingCycleStart()))
        .setBillingCyclePlannedEnd(
            DateTimeUtil.utcTimeToEpochSecs(userPlan.getBillingCyclePlannedEnd()));
    return ResponseEntity.ok(usersPlanResponse);
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
}
