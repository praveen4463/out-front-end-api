package com.zylitics.front.api;

import com.google.cloud.storage.Storage;
import com.zylitics.front.config.APICoreProperties;
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
  
  private final APICoreProperties.Storage storageProps;
  
  @Autowired
  public IssueController(Storage storage, APICoreProperties apiCoreProperties) {
    this.storage = storage;
    this.storageProps = apiCoreProperties.getStorage();
  }
  
  @SuppressWarnings("unused")
  @PutMapping
  public ResponseEntity<Void> uploadFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("fileName") String fileName,
                                         @RequestHeader(USER_INFO_REQ_HEADER) String userInfo)
      throws Exception {
    int userId = getUserId(userInfo);
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
    System.out.println(sendIssueRequest.getFileName() +
        sendIssueRequest.getDesc() + userId + " issue is sent");
    // TODO: send an email with subject: Issue report send by user: USER_ID
    //  and in body the desc and in the end:
    //  attached file (if available): FILE_NAME
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
