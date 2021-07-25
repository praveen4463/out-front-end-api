package com.zylitics.front.api;

import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.PlainTextEmail;
import com.zylitics.front.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("${app-short-version}/invitationRequest")
public class InvitationRequestController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(InvitationRequestController.class);
  
  private final APICoreProperties apiCoreProperties;
  
  private final EmailService emailService;
  
  @Autowired
  public InvitationRequestController(APICoreProperties apiCoreProperties,
                                     EmailService emailService) {
    this.apiCoreProperties = apiCoreProperties;
    this.emailService = emailService;
  }
  
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<Void> newInvitation(
      @RequestBody @Validated NewInvitationRequest newInvitationRequest) {
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    String userEmail = newInvitationRequest.getEmail();
    emailService.sendAsync(new PlainTextEmail()
        .setFrom(emailProps.getAppInternalEmailSender())
        .setTo(emailProps.getSupportEmail())
        .setSubject("New beta testing invitation request")
        .setContent(userEmail),
        null,
        (v) -> LOG.error("Priority: Couldn't send a email for new beta testing invitation: " +
            userEmail));
    // even if we couldn't send email, send a success as we'd record an error and read that.
    return ResponseEntity.ok().build();
  }
  
  @Validated
  private static class NewInvitationRequest {
    
    @NotBlank
    private String email;
  
    public String getEmail() {
      return email;
    }
  
    public void setEmail(String email) {
      this.email = email;
    }
  }
}
