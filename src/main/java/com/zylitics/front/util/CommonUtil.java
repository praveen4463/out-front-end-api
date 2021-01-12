package com.zylitics.front.util;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.zylitics.front.model.RunError;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
  
  public static RowMapper<Integer> getSingleInt() {
    return ((rs, rowNum) -> rs.getInt(1));
  }
  
  public static RowMapper<Long> getSingleLong() {
    return ((rs, rowNum) -> rs.getLong(1));
  }
  
  public static RowMapper<String> getSingleString() {
    return ((rs, rowNum) -> rs.getString(1));
  }
  
  public static RunError.LineInfo getLineInfo(String lineCh) {
    String[] parts = lineCh.split(":");
    return new RunError.LineInfo()
        .setLine(Integer.parseInt(parts[0]))
        .setCh(Integer.parseInt(parts[1]));
  }
  
  public static List<Integer> commaDelToNumericList(String commaDelimitedInt) {
    if (Strings.isNullOrEmpty(commaDelimitedInt)) {
      return new ArrayList<>(0);
    }
    return Splitter.on(",").omitEmptyStrings().trimResults()
        .splitToList(commaDelimitedInt).stream().map(Integer::parseInt).collect(Collectors.toList());
  }
}
