package com.zylitics.front.services;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.ASM;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.zylitics.front.SecretsManager;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.EmailInfo;
import com.zylitics.front.model.PlainTextEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class EmailService {
  
  private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);
  
  private static final int RESPONSE_TIMEOUT_SEC = 30;
  
  private static final String API_BASE_PATH = "api.sendgrid.com/v3";
  
  private static final String MAIL_SEND_ENDPOINT = "/mail/send";
  
  private final WebClient webClient;
  
  // !! I've not used sendGrid's library as SendGrid class wasn't thread safe.
  // TODO: for now no rate limiting is considered. Do that once you see those errors, refer
  //  BaseInterface class in SendGrid java client
  public EmailService(WebClient.Builder webClientBuilder,
                      APICoreProperties apiCoreProperties,
                      SecretsManager secretsManager) {
    APICoreProperties.Services services = apiCoreProperties.getServices();
    String secret =
        secretsManager.getSecretAsPlainText(services.getSendgridApiKeySecretCloudFile());
    String authHeader = "Bearer " + secret;
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(RESPONSE_TIMEOUT_SEC));
    this.webClient = webClientBuilder
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .baseUrl("https://" + API_BASE_PATH)
        .defaultHeader("Authorization", authHeader)
        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
  
  public boolean send(PlainTextEmail plain) {
    Mail mail = new Mail(
        new Email(plain.getFrom()),
        plain.getSubject(),
        new Email(plain.getTo()),
        new Content(MediaType.TEXT_PLAIN_VALUE, plain.getContent()));
    return send(mail);
  }
  
  public boolean send(SendTemplatedEmail sendTemplatedEmail) {
    Mail mail = new Mail();
    EmailInfo emailInfo = sendTemplatedEmail.getEmailInfo();
    mail.setFrom(new Email(emailInfo.getFrom(), emailInfo.getFromName()));
    mail.setTemplateId(sendTemplatedEmail.getTemplateId());
    if (sendTemplatedEmail.getUnsubscribeGroupId() != null) {
      ASM asm = new ASM();
      asm.setGroupId(sendTemplatedEmail.getUnsubscribeGroupId());
      if (sendTemplatedEmail.getUnsubscribeGroupsToShow() != null) {
        asm.setGroupsToDisplay(sendTemplatedEmail.getUnsubscribeGroupsToShow());
      }
      mail.setASM(asm);
    }
    Personalization personalization = new Personalization();
    personalization.addTo(new Email(emailInfo.getTo(), emailInfo.getFromName()));
    Map<String, Object> templateData = sendTemplatedEmail.getTemplateData();
    if (templateData == null) {
      templateData = new HashMap<>();
    }
    // add default template data
    if (templateData.get("year") == null) {
      templateData.put("year", LocalDateTime.now().getYear());
    }
    templateData.forEach(personalization::addDynamicTemplateData);
    mail.addPersonalization(personalization);
    return send(mail);
  }
  
  private boolean send(Mail mail) {
    try {
      ResponseEntity<Void> response = webClient.post()
          .uri(MAIL_SEND_ENDPOINT)
          .bodyValue(mail.build())
          .retrieve().toBodilessEntity().block();
      Objects.requireNonNull(response);
      return isSuccess(response.getStatusCode());
    } catch (Throwable t) {
      try {
        LOG.error("Couldn't send plan text mail: " + mail.build(), t);
      } catch (IOException io) {
        LOG.error("error while constructing mail");
      }
      return false;
    }
  }
  
  private boolean isSuccess(HttpStatus status) {
    return status == HttpStatus.OK || status == HttpStatus.CREATED;
  }
}
