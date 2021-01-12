package com.zylitics.front.provider;

import com.zylitics.front.model.BuildIdentifier;
import com.zylitics.front.model.BuildRunConfig;
import com.zylitics.front.model.BuildVM;
import com.zylitics.front.model.User;

public interface BuildProvider {
  
  BuildIdentifier newBuild(BuildRunConfig config, long buildRequestId, User user, int projectId);
  
  void createAndUpdateVM(BuildVM buildVM, int buildId);
  
  void updateSession(String sessionId, int buildId);
}
