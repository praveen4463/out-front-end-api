package com.zylitics.front.provider;

import com.zylitics.front.model.TestVersion;
import com.zylitics.front.model.TestVersionRename;

import java.util.List;
import java.util.Map;

public interface TestVersionProvider {
  
  TestVersion newVersion(TestVersion testVersion, int userId);
  
  void renameVersion(int versionId, TestVersionRename testVersionRename, int userId);
  
  void updateCode(int versionId, String code, int userId);
  
  String getCode(int versionId, int userId);
  
  Map<Integer, String> getCodes(List<Integer> versionIds, int userId);
  
  boolean anyVersionHasBlankCode(List<Integer> versionIds, int userId);
  
  void captureVersions(List<Integer> versionIds, int buildId);
  
  void deleteVersion(int versionId, int userId);
}
