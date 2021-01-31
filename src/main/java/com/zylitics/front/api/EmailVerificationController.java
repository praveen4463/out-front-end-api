package com.zylitics.front.api;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.EmailVerificationProvider;
import com.zylitics.front.provider.OrganizationProvider;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// users in this api will be anonymous and they should be deleted at client side after they're converted
@RestController
@RequestMapping("${app-short-version}/emailVerification")
public class EmailVerificationController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(EmailVerificationController.class);
  
  private final APICoreProperties apiCoreProperties;
  
  private final EmailVerificationProvider emailVerificationProvider;
  
  private final UserProvider userProvider;
  
  private final OrganizationProvider organizationProvider;
  
  private final EmailService emailService;
  
  public EmailVerificationController(APICoreProperties apiCoreProperties,
                                     EmailVerificationProvider emailVerificationProvider,
                                     UserProvider userProvider,
                                     OrganizationProvider organizationProvider,
                                     EmailService emailService) {
    this.apiCoreProperties = apiCoreProperties;
    this.emailVerificationProvider = emailVerificationProvider;
    this.userProvider = userProvider;
    this.organizationProvider = organizationProvider;
    this.emailService = emailService;
  }
  
  // when multiple invites need to be send, client must call api repeatedly, it doesn't accept
  // a list to encourage parallel requests completing earlier than one by one
  @PostMapping
  public ResponseEntity<?> sendEmailVerification(
      @RequestBody @Validated EmailVerificationRequest emailVerificationRequest) {
    if (emailVerificationRequest.getOrganizationId() != null) {
      Preconditions.checkArgument(emailVerificationRequest.getSenderName() != null
              && emailVerificationRequest.getOrganizationName() != null
              && emailVerificationRequest.getRole() != null, "Insufficient arguments supplied" +
          " while sending verification email");
    }
    String email = emailVerificationRequest.getEmail();
    Preconditions.checkArgument(StringUtil.isValidEmail(email));
    // check whether given email is in system
    if (userProvider.userWithEmailExist(email)) {
      return sendError(HttpStatus.UNPROCESSABLE_ENTITY, "A user with this email already exists",
          EmailApiErrorCause.EMAIL_ALREADY_EXIST);
    }
    // put new user in email verification
    String code = UUID.randomUUID().toString();
    emailVerificationProvider.newEmailVerification(
        new NewEmailVerification(emailVerificationRequest.getEmail(),
            code,
            emailVerificationRequest.getEmailVerificationUserType(),
            emailVerificationRequest.getOrganizationId(),
            emailVerificationRequest.getRole()));
    // once successfully done, send an email
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    EmailInfo emailInfo = new EmailInfo()
        .setFrom(emailProps.getNoReplyEmailSender())
        .setTo(email);
    if (emailVerificationRequest.getSenderName() != null) {
      emailInfo.setFromName(emailVerificationRequest.getSenderName());
    }
    String ctaLink = String.format("%s?code=%s",
        apiCoreProperties.getFrontEndBaseUrl() + emailProps.getFinishSignupPage(), code);
    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
    builder.put(emailProps.getCtaLinkTag(), ctaLink);
    if (emailVerificationRequest.getOrganizationName() != null) {
      builder.put("organization", emailVerificationRequest.getOrganizationName());
    }
    Map<String, Object> templateData = builder.build();
    SendTemplatedEmail sendTemplatedEmail;
    switch (emailVerificationRequest.getEmailVerificationUserType()) {
      case IN_ORGANIZATION:
        sendTemplatedEmail = new SendTemplatedEmail(emailInfo, emailProps.getEmailTeamInviteTmpId(),
            templateData);
        break;
      case BETA_INVITEE:
        sendTemplatedEmail = new SendTemplatedEmail(emailInfo, emailProps.getEmailBetaInviteTmpId(),
            templateData, emailProps.getMarketingEmailGroupId(), null);
        break;
      case NORMAL:
        sendTemplatedEmail = new SendTemplatedEmail(emailInfo, emailProps.getEmailVerifyTmpId(),
            templateData);
        break;
      default:
        throw new RuntimeException("Couldn't identify emailVerificationUserType: " +
            emailVerificationRequest.getEmailVerificationUserType());
    }
    boolean result = emailService.send(sendTemplatedEmail);
    if (!result) {
      LOG.error("Priority: Couldn't send a verification email to email: " + email);
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
  public ResponseEntity<?> validateEmailVerification(@PathVariable @NotBlank String code) {
    Optional<EmailVerification> emailVerificationOptional =
        emailVerificationProvider.getEmailVerification(code);
    if (!emailVerificationOptional.isPresent()) {
      throw new IllegalArgumentException("Can't identify verification link, the code is invalid");
    }
    EmailVerification emailVerification = emailVerificationOptional.get();
    if (emailVerification.isUsed()) {
      return sendError(HttpStatus.FORBIDDEN, "This link can be used just once");
    }
    emailVerificationProvider.updateToUsed(emailVerification.getId());
    String organizationName = null;
    if (emailVerification.getOrganizationId() != null) {
      organizationName = organizationProvider.getOrganization(
          emailVerification.getOrganizationId())
          .orElseThrow(() -> new RuntimeException("Couldn't get organization for id: " +
              emailVerification.getOrganizationId())).getName();
    }
    return ResponseEntity.ok(new ValidateEmailVerificationResponse(emailVerification.getId(),
        emailVerification.getEmail(), organizationName));
  }
}
