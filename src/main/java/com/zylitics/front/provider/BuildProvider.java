package com.zylitics.front.provider;

import com.zylitics.front.model.*;

import java.util.Optional;

public interface BuildProvider {
  
  BuildIdentifier newBuild(BuildRunConfig config, long buildRequestId, User user, int projectId);
  
  CompletedBuildsSummaryWithPaging getCompletedBuildsSummaryWithPaging(
      CompletedBuildSummaryFilters completedBuildSummaryFilters,
      int pageSize,
      int projectId,
      int userId
  );
  
  void createAndUpdateVM(BuildVM buildVM, int buildId);
  
  void updateSession(String sessionId, int buildId);
  
  void verifyUsersBuild(int buildId, int userId);
  
  Optional<RunnerPreferences> getRunnerPrefs(int buildId, int userId);
  
  Optional<CompletedBuildDetails> getCompletedBuildDetails(int buildId, int userId);
  
  String getCapturedCode(int buildId, int versionId, int userId);
}
