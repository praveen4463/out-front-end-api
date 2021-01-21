package com.zylitics.front.provider;

import com.zylitics.front.model.*;

import java.util.Optional;

public interface BuildProvider {
  
  BuildIdentifier newBuild(BuildRunConfig config, long buildRequestId, User user, int projectId);
  
  void createAndUpdateVM(BuildVM buildVM, int buildId);
  
  void updateSession(String sessionId, int buildId);
  
  void verifyUsersBuild(int buildId, int userId);
  
  Optional<RunnerPreferences> getRunnerPrefs(int buildId, int userId);
  
  Optional<BuildBasicDetails> getBuildBasicDetails(int buildId, int userId);
}
