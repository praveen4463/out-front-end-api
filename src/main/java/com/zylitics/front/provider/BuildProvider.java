package com.zylitics.front.provider;

import com.zylitics.front.model.*;

import java.util.List;
import java.util.Optional;

public interface BuildProvider {
  
  int newBuild(BuildRunConfig config, long buildRequestId, User user, int projectId);
  
  int duplicateBuild(BuildReRunConfig buildReRunConfig,
                     int buildId,
                     long buildRequestId,
                     User user);
  
  Optional<Build> getBuild(int buildId, int userId);
  
  CompletedBuildsSummaryWithPaging getCompletedBuildsSummaryWithPaging(
      CompletedBuildSummaryFilters completedBuildSummaryFilters,
      int pageSize,
      int projectId,
      int userId
  );
  
  void createAndUpdateVM(BuildVM buildVM, int buildId);
  
  void updateSessionRequestStart(int buildId);
  
  void updateSession(String sessionId, int buildId);
  
  void updateOnSessionFailure(SessionFailureReason sessionFailureReason, String error,
                                    int buildId);
  
  void verifyUsersBuild(int buildId, int userId);
  
  Optional<RunnerPreferences> getRunnerPrefs(int buildId, int userId);
  
  Optional<CompletedBuildDetails> getCompletedBuildDetails(int buildId, int userId);
  
  String getCapturedCode(int buildId, int versionId, int userId);
  
  List<RunningBuild> getRunningBuilds(Integer after, int projectId, int userId);
  
  RunningBuildSummary getRunningBuildSummary(int buildId, int userId);
}
