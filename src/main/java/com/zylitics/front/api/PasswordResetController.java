package com.zylitics.front.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
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

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("${app-short-version}/passwordReset")
public class PasswordResetController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(PasswordResetController.class);
  
  private static final int LINK_VALIDITY_HOURS = 6;
  
  private final APICoreProperties apiCoreProperties;
  
  private final PasswordResetProvider passwordResetProvider;
  
  private final UserProvider userProvider;
  
  private final EmailService emailService;
  
  public PasswordResetController(APICoreProperties apiCoreProperties,
                                 PasswordResetProvider passwordResetProvider,
                                 UserProvider userProvider,
                                 EmailService emailService) {
    this.apiCoreProperties = apiCoreProperties;
    this.passwordResetProvider = passwordResetProvider;
    this.userProvider = userProvider;
    this.emailService = emailService;
  }
  
  /*
  This should only be used when user opts for 'forget password' and not when change password. In
  change password, client should ask current password and new password, re-authenticate with firebase
  using the current password and email, and if succeeded, invoke firebase.updatePassword
  When forget password is chosen, even if user has entered an email that doesn't exist, we don't
  show error but just tell user. they should get an email if this email is registered.
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
    String ctaLink = String.format("%s?code=%s",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getPwdResetPage(), code);
    Map<String, Object> templateData = ImmutableMap.of(emailProps.getCtaLinkTag(), ctaLink);
    SendTemplatedEmail sendTemplatedEmail = new SendTemplatedEmail(emailInfo,
        emailProps.getEmailPwdResetTmpId(), templateData);
    boolean result = emailService.send(sendTemplatedEmail);
    if (!result) {
      LOG.error("Priority: Couldn't send a password reset email to user: " + userId);
      String errMsg = "An error occurred while sending an email. There is no need to try again." +
          " Please wait for sometime and check your inbox. It" +
          " may take us a few hours to try resending it.";
      return sendError(HttpStatus.INTERNAL_SERVER_ERROR, errMsg,
          EmailApiErrorCause.EMAIL_SENDING_FAILED);
    }
    return ResponseEntity.ok().build();
  }
  
  @PatchMapping("/{code}/validate")
  public ResponseEntity<?> validatePasswordReset(@PathVariable @NotBlank String code) {
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
}
