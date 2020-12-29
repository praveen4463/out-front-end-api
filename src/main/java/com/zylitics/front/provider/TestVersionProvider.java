package com.zylitics.front.provider;

import com.zylitics.front.model.TestVersion;

public interface TestVersionProvider {
  
  TestVersion newVersion(TestVersion testVersion, int userId);
  
  void renameVersion(TestVersion testVersion, int userId);
  
  void deleteVersion(int versionId, int userId);
}
