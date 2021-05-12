package com.zylitics.front.api;

import com.zylitics.front.config.APICoreProperties;
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
  
  // Used for caching responses for shorter periods. Overtime a software's way of representing things
  // may change that will require a different type of response than it was before. Hence we will
  // use this cache such things.
  // Can also be used to cache things that don't change rapidly.
  public static ResponseEntity.BodyBuilder addShortTermCacheControl(
      ResponseEntity.BodyBuilder builder) {
    return builder.header("Cache-Control", "private, max-age=1200"); // 20m of cache, not immutable
  }
  
  // !!currently not considering dst for US/UK timezones
  // UTC + 5:30 offsetMinutes will be -330 and UTC - 8:00 will be 480
  // !!! All listed buckets must exist
  public static String getShotBucketPerOffset(int offsetMinutes,
                                              APICoreProperties apiCoreProperties) {
    APICoreProperties.Storage storage = apiCoreProperties.getStorage();
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
        return storage.getShotBucketAu();
      }
      if (hoursOffset == 9) {
        return storage.getShotBucketTok();
      }
      if (hoursOffset >= 8) {
        return storage.getShotBucketHk();
      }
      if (hoursOffset >= 4) {
        return storage.getShotBucketMum();
      }
      if (hoursOffset >= 1) {
        return storage.getShotBucketEu();
      }
      return storage.getShotBucketUk();
    }
    // we're on UTC - 01:00 and lesser timezones
    if (hoursOffset >= 8) {
      // UTC-8, Los Angeles, Las Vegas
      return storage.getShotBucketLa();
    }
    if (hoursOffset >= 7) {
      return storage.getShotBucketSlake();
    }
    if (hoursOffset >= 6) {
      return storage.getShotBucketUsc();
    }
    if (hoursOffset >= 4) {
      return storage.getShotBucketNv();
    }
    return storage.getShotBucketUsc(); // default is us-central
  }
  
  public static String getUserDisplayName(String firstName, String lastName) {
    return firstName + " " + lastName;
  }
}
