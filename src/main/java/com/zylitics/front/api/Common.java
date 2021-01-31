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
  
  // !!currently not considering dst for US/UK timezones
  // UTC + 5:30 offsetMinutes will be -330 and UTC - 8:00 will be 480
  // !!! All listed buckets must exist
  public static String getShotBucketPerOffset(int offsetMinutes) {
    boolean negativeOffset = offsetMinutes < 0; // keep whether we've a negative offset as we will
    // make it positive for easy calculations.
    double hoursOffset = (double) offsetMinutes / 60;
    if (negativeOffset) {
      // if this is negative offset, make it positive so that we can compare with positive numbers
      // ,this makes comparison easier
      hoursOffset *= -1;
    }
    if (negativeOffset || hoursOffset == 0) {
      // we're on UTC + 00:00 and greater timezones
      if (hoursOffset > 9) {
        return "zl-session-assets-au";
      }
      if (hoursOffset == 9) {
        return "zl-session-assets-tok";
      }
      if (hoursOffset >= 8) {
        return "zl-session-assets-hk";
      }
      if (hoursOffset >= 4) {
        return "zl-session-assets-mum";
      }
      if (hoursOffset >= 1) {
        return "zl-session-assets-eu";
      }
      return "zl-session-assets-uk";
    }
    // we're on UTC - 01:00 and lesser timezones
    if (hoursOffset >= 8) {
      // UTC-8, Los Angeles, Las Vegas
      return "zl-session-assets-la";
    }
    if (hoursOffset >= 7) {
      return "zl-session-assets-slake";
    }
    if (hoursOffset >= 6) {
      return "zl-session-assets-usc";
    }
    if (hoursOffset >= 4) {
      return "zl-session-assets-nv";
    }
    return "zl-session-assets-usc"; // default is us-central
  }
}
