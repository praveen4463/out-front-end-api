package com.zylitics.front.provider;

import com.zylitics.front.model.File;
import com.zylitics.front.model.FileIdentifier;

import java.util.List;

public interface FileProvider {
  
  File newFile(File file, int projectId, int userId);
  
  List<FileIdentifier> getFilesIdentifier(int projectId, int userId);
  
  List<File> getFilesWithTests(int projectId,
                               List<Integer> fileIdsFilter,
                               boolean excludeCode,
                               boolean excludeNoCodeTests,
                               boolean includeNoTestFiles,
                               int useId);
  
  void renameFile(File file, int projectId, int userId);
  
  void deleteFile(int fileId, int userId);
}
