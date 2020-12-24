package com.zylitics.front.util;

import com.google.common.base.Strings;

import javax.annotation.Nullable;

public class CommonUtil {
  
  public static String constructStorageFilePath(@Nullable String prefix, String fileName) {
    if (Strings.isNullOrEmpty(prefix)) {
      return fileName;
    }
    return prefix + (prefix.endsWith("/") ? "" : "/") + fileName;
  }
  
  public static String getStorageFileNameWithoutPrefix(String fileName) {
    int index = fileName.lastIndexOf("/");
    if (index < 0) {
      return fileName;
    }
    return fileName.substring(index + 1);
  }
  
  public static String replaceUserId(String from, int userId) {
    return from.replace("USER_ID", String.valueOf(userId));
  }
  
  public static void validateSingleRowDbCommit(int result) {
    if (result != 1) {
      throw new RuntimeException("Expected one row to be affected but it was " + result);
    }
  }
}
