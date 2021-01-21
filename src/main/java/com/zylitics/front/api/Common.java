package com.zylitics.front.api;

import org.springframework.http.ResponseEntity;

public class Common {
  
  public static String getBuildDirName(int buildId) {
    return "build-" + buildId;
  }
  
  // The directory structure in cloud store should be like:
  // Build_Dir/Asset_Dir/Actual_File
  public static String getBlobName(String buildDirName, String parentDirName, String fileName) {
    return buildDirName + "/" + parentDirName + "/" + fileName;
  }
  
  public static String getBlobName(String buildDirName, String fileName) {
    return buildDirName + "/" + fileName;
  }
  
  public static ResponseEntity.BodyBuilder addCacheControlPublic(
      ResponseEntity.BodyBuilder builder) {
    return builder.header("Cache-Control", "public, max-age=604800, immutable");
  }
}
