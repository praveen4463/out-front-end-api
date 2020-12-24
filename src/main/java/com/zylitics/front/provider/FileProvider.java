package com.zylitics.front.provider;

import com.zylitics.front.model.File;
import com.zylitics.front.model.FileIdentifier;

import java.util.List;

public interface FileProvider {
  
  List<FileIdentifier> getFilesIdentifier(int projectId, int userId);
  
  List<File> getFilesWithTests(List<Integer> fileIdsFilter, int useId);
}
