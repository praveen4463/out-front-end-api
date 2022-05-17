package com.zylitics.front.api;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.exception.UnauthorizedException;
import com.zylitics.front.model.TestFile;
import com.zylitics.front.model.User;
import com.zylitics.front.provider.UserProvider;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.front.util.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("${app-short-version}/testFiles")
public class TestFileController extends AbstractController {
  
  private final Storage storage;
  
  private final APICoreProperties.Storage storageProps;
  
  private final UserProvider userProvider;
  
  @Autowired
  public TestFileController(Storage storage,
                            UserProvider userProvider,
                            APICoreProperties apiCoreProperties) {
    this.storage = storage;
    this.userProvider = userProvider;
    this.storageProps = apiCoreProperties.getStorage();
  }
  
  @SuppressWarnings("unused")
  @PutMapping
  public ResponseEntity<Void> uploadFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("fileName") String fileName,
                                         @RequestHeader(USER_INFO_REQ_HEADER) String userInfo)
      throws Exception {
    int userId = getUserId(userInfo);
    User user = userProvider.getUser(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));
    new FileUpload(storage, storageProps.getUserDataBucket(), file, fileName,
        getFilePrefix(storageProps, user.getOrganizationId()),
        storageProps.getMaxTestFileSizeMb()).upload();
    return ResponseEntity.ok().build();
  }
  
  @GetMapping
  public ResponseEntity<List<TestFile>> getFiles(
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    User user = userProvider.getUser(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));
    Page<Blob> blobs = storage.list(storageProps.getUserDataBucket(),
        Storage.BlobListOption.currentDirectory(),
        Storage.BlobListOption.prefix(getFilePrefix(storageProps, user.getOrganizationId()) + "/"));
    Iterator<Blob> blobIterator = blobs.iterateAll().iterator();
    List<TestFile> files = new ArrayList<>();
    while (blobIterator.hasNext()) {
      Blob blob = blobIterator.next();
      LocalDateTime createDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(blob.getCreateTime()),
          ZoneId.of("UTC"));
      TestFile file = new TestFile()
          .setName(CommonUtil.getStorageFileNameWithoutPrefix(blob.getName()))
          .setCreateDate(createDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
          .setSize(IOUtil.getBytesStringRepresentation(blob.getSize()));
      files.add(file);
    }
    return ResponseEntity.ok(files);
  }
  
  @GetMapping("/{fileName}")
  public ResponseEntity<byte[]> getFile(@PathVariable String fileName,
                                        @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    User user = userProvider.getUser(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));
    Blob blob = storage.get(BlobId.of(storageProps.getUserDataBucket(),
        CommonUtil.constructStorageFilePath(getFilePrefix(storageProps, user.getOrganizationId())
            , fileName)));
    Preconditions.checkNotNull(blob, fileName + " doesn't exists");
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + fileName + "\"")
        .body(new FileDownload().download(blob));
  }
  
  @DeleteMapping("/{fileName}")
  public ResponseEntity<Void> deleteFile(@PathVariable String fileName,
                                         @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    User user = userProvider.getUser(userId)
        .orElseThrow(() -> new UnauthorizedException("User not found"));
    boolean deleted = storage.delete(BlobId.of(storageProps.getUserDataBucket(),
        CommonUtil.constructStorageFilePath(getFilePrefix(storageProps, user.getOrganizationId()),
            fileName)));
    Preconditions.checkArgument(deleted, fileName + " doesn't exists");
    return ResponseEntity.ok().build();
  }
  
  private String getFilePrefix(APICoreProperties.Storage storageProps, int orgId) {
    return CommonUtil.replaceUserId(storageProps.getUserUploadsStorageDirTmpl(), orgId);
  }
}
