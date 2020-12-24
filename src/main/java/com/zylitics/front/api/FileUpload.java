package com.zylitics.front.api;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.zylitics.front.util.CommonUtil;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.regex.Pattern;

class FileUpload {
  
  private static final int GCP_OBJECT_NAME_MAX_LENGTH = 500;
  
  private static final String GCP_OBJECT_NAME_REGEX = "[\\\\/\\s*\\[\\]?#]|^\\.$|^\\.\\.$";
  
  private final Storage storage;
  
  private final String bucket;
  
  private final MultipartFile file;
  
  private final String fileName;
  
  private final String filePrefix;
  
  private final int maxSizeMB;
  
  FileUpload(Storage storage, String bucket, MultipartFile file, String fileName,
             @Nullable String filePrefix, int maxSizeMB) {
    this.storage = storage;
    this.bucket = bucket;
    this.file = file;
    this.fileName = fileName;
    this.filePrefix = filePrefix;
    this.maxSizeMB = maxSizeMB;
  }
  
  void upload() throws Exception {
    // validate file props, don't elaborate errors as this api is internal and meant to be used via
    // our front end only that already has validations.
    Preconditions.checkArgument(!file.isEmpty(), "File is empty");
    byte[] bytes = file.getBytes();
    Preconditions.checkArgument(bytes.length <= maxSizeMB * 1048576
        , "File is too big");
    Preconditions.checkArgument(fileName.length() <= GCP_OBJECT_NAME_MAX_LENGTH,
        "File name is too long");
    Preconditions.checkArgument(!Pattern.compile(GCP_OBJECT_NAME_REGEX).matcher(fileName).find(),
        "File name has invalid characters");
    
    BlobInfo blobInfo = BlobInfo.newBuilder(bucket,
        CommonUtil.constructStorageFilePath(filePrefix, fileName))
        .setContentType(file.getContentType())
        .setCacheControl("public, max-age=604800, immutable").build();
    // we don't need reattempts, if it fails user may try again.
    try (WriteChannel writer = storage.writer(blobInfo)) {
      writer.write(ByteBuffer.wrap(bytes, 0, bytes.length));
    }
  }
}
