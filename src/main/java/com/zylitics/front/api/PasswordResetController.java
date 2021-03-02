package com.zylitics.front.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.PasswordResetProvider;
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
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("${app-short-version}/passwordResets")
public class PasswordResetController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(PasswordResetController.class);
  
  private static final int LINK_VALIDITY_HOURS = 6;
  
  private final APICoreProperties apiCoreProperties;
  
  private final PasswordResetProvider passwordResetProvider;
  
  private final UserProvider userProvider;
  
  private final FirebaseAuth firebaseAuth;
  
  private final EmailService emailService;
  
  public PasswordResetController(APICoreProperties apiCoreProperties,
                                 PasswordResetProvider passwordResetProvider,
                                 UserProvider userProvider,
                                 FirebaseAuth firebaseAuth,
                                 EmailService emailService) {
    this.apiCoreProperties = apiCoreProperties;
    this.passwordResetProvider = passwordResetProvider;
    this.userProvider = userProvider;
    this.firebaseAuth = firebaseAuth;
    this.emailService = emailService;
  }
  
  /*
  This should only be used when user opts for 'forget password' and not when change password. In
  change password, client should ask current password and new password, re-authenticate with firebase
  using the current password and email, and if succeeded, invoke firebase.updatePassword
  When forget password is chosen, even if user has entered an email that doesn't exist, we don't
  show error but just tell user. they should get an email if this email is registered.
  
  Password resets could be done when user is logged in or out and thus I've not take userInfo header
  or checked anonymous user.
   */
  @PostMapping
  public ResponseEntity<?> sendPasswordReset(
      @RequestBody @Validated PasswordResetRequest passwordResetRequest) {
    String email = passwordResetRequest.getEmail();
    Preconditions.checkArgument(StringUtil.isValidEmail(email));
    Optional<Integer> userIdOptional = userProvider.getUserId(email);
    // check whether given email is in system
    if (!userIdOptional.isPresent()) {
      LOG.warn("Given email for password reset has no user: " + email);
      return ResponseEntity.ok().build();
    }
    int userId = userIdOptional.get();
    String code = UUID.randomUUID().toString();
    passwordResetProvider.newPasswordReset(new NewPasswordReset(userId, code));
    // once successfully done, send an email
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    EmailInfo emailInfo = new EmailInfo()
        .setFrom(emailProps.getNoReplyEmailSender())
        .setTo(email);
    String ctaLink = String.format("%s/%s",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getPwdResetPage(), code);
    Map<String, Object> templateData = ImmutableMap.of(emailProps.getCtaLinkTag(), ctaLink);
    SendTemplatedEmail sendTemplatedEmail = new SendTemplatedEmail(emailInfo,
        emailProps.getEmailPwdResetTmpId(), templateData);
    boolean result = emailService.send(sendTemplatedEmail);
    if (!result) {
      LOG.error("Priority: Couldn't send a password reset email to user: " + userId);
      // we tried sending to an existing email but failed that's why we ask user not to try again
      // rather than asking 'if you think email is incorrect...'
      String errMsg = "An error occurred while sending an email. There is no need to try again." +
          " Please wait for sometime and check your inbox. It" +
          " may take us a few hours to try resending it.";
      return sendError(HttpStatus.INTERNAL_SERVER_ERROR, errMsg,
          EmailApiErrorCause.EMAIL_SENDING_FAILED);
    }
    return ResponseEntity.ok().build();
  }
  
  @PatchMapping("/{code}/validate")
  public ResponseEntity<?> validatePasswordReset(
      @PathVariable @NotBlank String code) {
    Optional<PasswordReset> passwordResetOptional =
        passwordResetProvider.getPasswordReset(code);
    if (!passwordResetOptional.isPresent()) {
      throw new IllegalArgumentException("Can't identify password reset link, the code is invalid");
    }
    PasswordReset passwordReset = passwordResetOptional.get();
    if (passwordReset.isUsed()) {
      return sendError(HttpStatus.FORBIDDEN, "This link can be used just once");
    }
    if (ChronoUnit.HOURS.between(passwordReset.getCreateDate(), LocalDateTime.now())
        > LINK_VALIDITY_HOURS) {
      return sendError(HttpStatus.FORBIDDEN, "This link has been expired");
    }
    passwordResetProvider.updateToUsed(passwordReset.getId());
    return ResponseEntity.ok(new ValidatePasswordResetResponse(passwordReset.getId(),
        userProvider.getUserEmail(passwordReset.getUserId())));
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
    String password = resetPasswordRequest.getPassword().trim();
    Preconditions.checkArgument(password.length() >= 6, "Password requirement not met");
    try {
      firebaseAuth.updateUser(new UserRecord.UpdateRequest(
          Integer.toString(passwordReset.getUserId()))
          .setPassword(password));
    } catch (FirebaseAuthException f) {
      LOG.error("Priority: Couldn't reset user password in firebase, userId: " +
          passwordReset.getUserId(), f);
      return sendError(HttpStatus.INTERNAL_SERVER_ERROR, "There was an error resetting your" +
          " password. We've been notified and this should be fixed very soon. Please try" +
          " resetting again in a few hours or contact us if you see a similar problem.");
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
