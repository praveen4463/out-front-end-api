package com.zylitics.front.api;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.TestFile;
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
  
  @Autowired
  public TestFileController(Storage storage, APICoreProperties apiCoreProperties) {
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
    new FileUpload(storage, storageProps.getUserDataBucket(), file, fileName,
        getFilePrefix(storageProps, userId),
        storageProps.getMaxTestFileSizeMb()).upload();
    return ResponseEntity.ok().build();
  }
  
  @GetMapping
  public ResponseEntity<List<TestFile>> getFiles(
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    Page<Blob> blobs = storage.list(storageProps.getUserDataBucket(),
        Storage.BlobListOption.currentDirectory(),
        Storage.BlobListOption.prefix(getFilePrefix(storageProps, userId) + "/"));
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
    Blob blob = storage.get(BlobId.of(storageProps.getUserDataBucket(),
        CommonUtil.constructStorageFilePath(getFilePrefix(storageProps, userId), fileName)));
    Preconditions.checkNotNull(blob, fileName + " doesn't exists");
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
        "attachment; filename=\"" + fileName + "\"")
        .body(blob.getContent());
  }
  
  @DeleteMapping("/{fileName}")
  public ResponseEntity<Void> deleteFile(@PathVariable String fileName,
                                         @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    boolean deleted = storage.delete(BlobId.of(storageProps.getUserDataBucket(),
        CommonUtil.constructStorageFilePath(getFilePrefix(storageProps, userId), fileName)));
    Preconditions.checkArgument(deleted, fileName + " doesn't exists");
    return ResponseEntity.ok().build();
  }
  
  private String getFilePrefix(APICoreProperties.Storage storageProps, int userId) {
    return CommonUtil.replaceUserId(storageProps.getUserUploadsStorageDirTmpl(), userId);
  }
}
