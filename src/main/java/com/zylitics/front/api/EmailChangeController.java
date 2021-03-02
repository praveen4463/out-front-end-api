package com.zylitics.front.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.EmailChangeProvider;
import com.zylitics.front.provider.UserProvider;
import com.zylitics.front.services.EmailService;
import com.zylitics.front.services.SendTemplatedEmail;
import com.zylitics.front.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("${app-short-version}/emailChanges")
public class EmailChangeController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(PasswordResetController.class);
  
  private static final int LINK_VALIDITY_HOURS = 6;
  
  private final APICoreProperties apiCoreProperties;
  
  private final EmailChangeProvider emailChangeProvider;
  
  private final UserProvider userProvider;
  
  private final FirebaseAuth firebaseAuth;
  
  private final EmailService emailService;
  
  public EmailChangeController(APICoreProperties apiCoreProperties,
                               EmailChangeProvider emailChangeProvider,
                               UserProvider userProvider,
                               FirebaseAuth firebaseAuth,
                               EmailService emailService) {
    this.apiCoreProperties = apiCoreProperties;
    this.emailChangeProvider = emailChangeProvider;
    this.userProvider = userProvider;
    this.firebaseAuth = firebaseAuth;
    this.emailService = emailService;
  }
  
  // Email change is always initiated when user is logged in so we've the real user credentials.
  @PostMapping
  public ResponseEntity<?> sendEmailChange(
      @RequestBody @Validated EmailChangeRequest emailChangeRequest,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    String previousEmail = userProvider.getUserEmail(userId);
    String newEmail = emailChangeRequest.getNewEmail();
    Preconditions.checkArgument(!previousEmail.equalsIgnoreCase(newEmail),
        "New email can't be same as the current email");
    Preconditions.checkArgument(StringUtil.isValidEmail(newEmail));
    String code = UUID.randomUUID().toString();
    emailChangeProvider.newEmailChange(new NewEmailChange(previousEmail, newEmail, code, userId));
    // once successfully done, send an email
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    EmailInfo emailInfo = new EmailInfo()
        .setFrom(emailProps.getNoReplyEmailSender())
        .setTo(newEmail);
    String ctaLink = String.format("%s/%s",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getEmailChangePage(), code);
    Map<String, Object> templateData = ImmutableMap.of(emailProps.getCtaLinkTag(), ctaLink);
    SendTemplatedEmail sendTemplatedEmail = new SendTemplatedEmail(emailInfo,
        emailProps.getEmailChangeTmpId(), templateData);
    boolean result = emailService.send(sendTemplatedEmail);
    if (!result) {
      LOG.error("Priority: Couldn't send a email change email to user: " + userId);
      String errMsg = "An error occurred while sending an email. If you think the email is" +
          " invalid, try again with" +
          " a valid one otherwise please wait for sometime and check your inbox. It" +
          " may take us a few hours to try resending it.";
      return sendError(HttpStatus.INTERNAL_SERVER_ERROR, errMsg,
          EmailApiErrorCause.EMAIL_SENDING_FAILED);
    }
    return ResponseEntity.ok().build();
  }
  
  @PatchMapping("/{code}/validate")
  public ResponseEntity<?> validateEmailChange(
      @PathVariable @NotBlank String code,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    Optional<EmailChange> emailChangeOptional = emailChangeProvider.getEmailChange(code);
    if (!emailChangeOptional.isPresent()) {
      throw new IllegalArgumentException("Can't identify email change link, the code is invalid");
    }
    EmailChange emailChange = emailChangeOptional.get();
    if (emailChange.getUserId() != userId) {
      return sendError(HttpStatus.UNAUTHORIZED, "User isn't authorized for this operation");
    }
    if (emailChange.isUsed()) {
      return sendError(HttpStatus.FORBIDDEN, "This link can be used just once");
    }
    if (ChronoUnit.HOURS.between(emailChange.getCreateDate(), LocalDateTime.now())
        > LINK_VALIDITY_HOURS) {
      return sendError(HttpStatus.FORBIDDEN, "This link has been expired");
    }
    emailChangeProvider.updateToUsed(emailChange.getId());
    return ResponseEntity.ok(emailChange.getId()); // send back only emailChangeId
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
}
