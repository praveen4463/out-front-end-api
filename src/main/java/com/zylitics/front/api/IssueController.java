package com.zylitics.front.api;

import com.google.cloud.storage.Storage;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.EmailInfo;
import com.zylitics.front.model.PlainTextEmail;
import com.zylitics.front.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@RestController
@RequestMapping("${app-short-version}/issue")
public class IssueController extends AbstractController {
  
  private final Storage storage;
  
  private final APICoreProperties apiCoreProperties;
  
  private final EmailService emailService;
  
  @Autowired
  public IssueController(Storage storage,
                         APICoreProperties apiCoreProperties,
                         EmailService emailService) {
    this.storage = storage;
    this.apiCoreProperties = apiCoreProperties;
    this.emailService = emailService;
  }
  
  @SuppressWarnings("unused")
  @PutMapping
  public ResponseEntity<Void> uploadFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("fileName") String fileName,
                                         @RequestHeader(USER_INFO_REQ_HEADER) String userInfo)
      throws Exception {
    int userId = getUserId(userInfo);
    APICoreProperties.Storage storageProps = apiCoreProperties.getStorage();
    // we'll append a uuid to fileName to keep it unique within a user dir but will send just
    // file name with the email as client is unknown about the added uuid.
    new FileUpload(storage, storageProps.getCommonUploadsBucket(), file,
        fileName + UUID.randomUUID(), getFilePrefix(userId),
        storageProps.getMaxCommonFileSizeMb()).upload();
    return ResponseEntity.ok().build();
  }
  
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<Void> sendIssue(
      @RequestBody @Validated SendIssueRequest sendIssueRequest,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    APICoreProperties.Email emailProps = apiCoreProperties.getEmail();
    emailService.send(new PlainTextEmail()
        .setFrom(emailProps.getAppInternalEmailSender())
        .setTo(emailProps.getIssueReportReceiver())
        .setSubject("Issue report sent by user: " + userId)
        .setContent(sendIssueRequest.getDesc() +
            "\nAttached file (if any): " + sendIssueRequest.getFileName()));
    // even if we couldn't send email, send a success as we'd record an error and read that.
    return ResponseEntity.ok().build();
  }
  
  private String getFilePrefix(int userId) {
    return String.valueOf(userId);
  }
  
  @Validated
  private static class SendIssueRequest {
    
    @NotBlank
    private String desc;
    
    private String fileName;
  
    public String getDesc() {
      return desc;
    }
  
    @SuppressWarnings("unused")
    public SendIssueRequest setDesc(String desc) {
      this.desc = desc;
      return this;
    }
  
    public String getFileName() {
      return fileName;
    }
  
    @SuppressWarnings("unused")
    public SendIssueRequest setFileName(String fileName) {
      this.fileName = fileName;
      return this;
    }
  }
}
