package com.zylitics.front.dao;

import com.zylitics.front.model.*;
import com.zylitics.front.provider.*;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.front.util.DateTimeUtil;
import com.zylitics.front.util.Randoms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DaoBuildProvider extends AbstractDaoProvider implements BuildProvider {
  
  private final TransactionTemplate transactionTemplate;
  
  private final Randoms randoms;
  
  private final Common common;
  
  private final BuildCapabilityProvider buildCapabilityProvider;
  
  private final TestVersionProvider testVersionProvider;
  
  private final BuildVarProvider buildVarProvider;
  
  private final GlobalVarProvider globalVarProvider;
  
  private final BuildVMProvider buildVMProvider;
  
  @Autowired
  DaoBuildProvider(NamedParameterJdbcTemplate jdbc,
                   TransactionTemplate transactionTemplate,
                   Randoms randoms,
                   Common common,
                   BuildCapabilityProvider buildCapabilityProvider,
                   TestVersionProvider testVersionProvider,
                   BuildVarProvider buildVarProvider,
                   GlobalVarProvider globalVarProvider,
                   BuildVMProvider buildVMProvider) {
    super(jdbc);
    this.transactionTemplate = transactionTemplate;
    this.randoms = randoms;
    this.common = common;
    this.buildCapabilityProvider = buildCapabilityProvider;
    this.testVersionProvider = testVersionProvider;
    this.buildVarProvider = buildVarProvider;
    this.globalVarProvider = globalVarProvider;
    this.buildVMProvider = buildVMProvider;
  }
  
  @Override
  public BuildIdentifier newBuild(BuildRunConfig config,
                                  long buildRequestId,
                                  User user,
                                  int projectId) {
    common.verifyUsersProject(projectId, user.getId());
    return transactionTemplate.execute(ts -> newBuildInTransaction(config, buildRequestId, user,
        projectId));
  }
  
  private BuildIdentifier newBuildInTransaction(BuildRunConfig config,
                                                long buildRequestId,
                                                User user,
                                                int projectId) {
    String sql = "INSERT INTO bt_build\n" +
        "(build_key, server_screen_size, server_timezone_with_dst,\n" +
        "shot_bucket_session_storage, abort_on_failure, aet_keep_single_window,\n" +
        "aet_update_url_blank, aet_reset_timeouts, aet_delete_all_cookies, bt_project_id,\n" +
        "source_type, bt_build_request_id, create_date)\n" +
        "VALUES (:build_key, :server_screen_size, :server_timezone_with_dst,\n" +
        ":shot_bucket_session_storage, :abort_on_failure, :aet_keep_single_window,\n" +
        ":aet_update_url_blank, :aet_reset_timeouts, :aet_delete_all_cookies, :bt_project_id,\n" +
        ":source_type, :bt_build_request_id, :create_date) RETURNING bt_build_id";
    RunnerPreferences runnerPreferences = config.getRunnerPreferences();
    // TODO: currently I'm generating and putting a random without checking for existence, it may
    //  fail when duplicate. Keep and eye and fix later.
    String buildKey = randoms.generateRandom(10);
    int buildId = jdbc.query(sql, new SqlParamsBuilder()
        .withVarchar("build_key", buildKey)
        .withVarchar("server_screen_size", config.getDisplayResolution())
        .withOther("server_timezone_with_dst", config.getTimezone())
        .withVarchar("shot_bucket_session_storage", user.getShotBucketSessionStorage())
        .withBoolean("abort_on_failure", runnerPreferences.isAbortOnFailure())
        .withBoolean("aet_keep_single_window", runnerPreferences.isAetKeepSingleWindow())
        .withBoolean("aet_update_url_blank", runnerPreferences.isAetUpdateUrlBlank())
        .withBoolean("aet_reset_timeouts", runnerPreferences.isAetResetTimeouts())
        .withBoolean("aet_delete_all_cookies", runnerPreferences.isAetDeleteAllCookies())
        .withOther("source_type", config.getBuildSourceType())
        .withBigint("bt_build_request_id", buildRequestId)
        .withProject(projectId)
        .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
    
    buildCapabilityProvider.captureCapability(config.getBuildCapabilityId(), buildId);
    
    testVersionProvider.captureVersions(config.getVersionIds(), buildId);
    
    buildVarProvider.capturePrimaryBuildVarsOverridingGiven(projectId,
        config.getSelectedBuildVarIdPerKey(),
        buildId);
    
    globalVarProvider.captureGlobalVars(projectId, buildId);
    
    return new BuildIdentifier().setBuildId(buildId).setBuildKey(buildKey);
  }
  
  @Override
  public CompletedBuildsSummaryWithPaging getCompletedBuildsSummaryWithPaging(
      CompletedBuildSummaryFilters filters,
      int pageSize,
      int projectId,
      int userId) {
    // - With clause doesn't fetch all the rows and limits itself based on parent's query.
    // - start and end dates in filters are in UTC and converted to OffsetDates, db already has dates
    // stored in utc, so when pg compares, it won't convert our dates to UTC and UTC - UTC comparison
    // will happen.
    String sql = "WITH per_status_count AS (\n" +
        "SELECT bt_build_id,\n" +
        "sum(CASE WHEN status=:success THEN 1 ELSE 0 END) AS total_success,\n" +
        "sum(CASE WHEN status=:error THEN 1 ELSE 0 END) AS total_error,\n" +
        "sum(CASE WHEN status=:stopped THEN 1 ELSE 0 END) AS total_stopped,\n" +
        "sum(CASE WHEN status=:aborted THEN 1 ELSE 0 END) AS total_aborted\n" +
        "FROM bt_build_status GROUP BY bt_build_id\n" +
        ")\n" +
        "SELECT bt_build_id, bu.name, bu.final_status, bu.error, bu.source_type,\n" +
        "bu.create_date AT TIME ZONE 'UTC' AS create_date,\n" +
        "EXTRACT(MILLISECONDS FROM (bu.end_date - bu.start_date)) AS test_time,\n" +
        "total_success, total_error, total_stopped, total_aborted,\n" +
        "server_os, wd_browser_name\n" +
        "FROM bt_build bu JOIN per_status_count USING (bt_build_id)\n" +
        "JOIN bt_build_captured_capabilities USING (bt_build_id)\n" +
        "JOIN bt_project p USING (bt_project_id)\n" +
        "WHERE p.zluser_id = :zluser_id AND p.bt_project_id = :bt_project_id\n" +
        "AND bu.final_status IS NOT NULL\n" +
        "AND bu.create_date >= :start_date AND bu.create_date <= :end_date\n";
    SqlParamsBuilder paramsBuilder = new SqlParamsBuilder(projectId, userId);
    paramsBuilder.withOther("success", TestStatus.SUCCESS);
    paramsBuilder.withOther("error", TestStatus.ERROR);
    paramsBuilder.withOther("stopped", TestStatus.STOPPED);
    paramsBuilder.withOther("aborted", TestStatus.ABORTED);
    paramsBuilder.withTimestampTimezone("start_date", filters.getStartDateUTC());
    paramsBuilder.withTimestampTimezone("end_date", filters.getEndDateUTC());
    if (filters.getFinalStatus() != null) {
      sql += "AND bu.final_status = :final_status\n";
      paramsBuilder.withOther("final_status", filters.getFinalStatus());
    }
    if (filters.getBrowserName() != null) {
      sql += "AND wd_browser_name = :wd_browser_name\n";
      paramsBuilder.withVarchar("wd_browser_name", filters.getBrowserName());
    }
    if (filters.getBrowserVersion() != null) {
      sql += "AND wd_browser_version = :wd_browser_version\n";
      paramsBuilder.withVarchar("wd_browser_version", filters.getBrowserVersion());
    }
    if (filters.getOs() != null) {
      sql += "AND server_os = :server_os\n";
      paramsBuilder.withVarchar("server_os", filters.getOs());
    }
    if (filters.getBeforeBuildId() != null) {
      sql += "AND bu.bt_build_id < :before\n";
      paramsBuilder.withInteger("before", filters.getBeforeBuildId());
    }
    if (filters.getAfterBuildId() != null) {
      sql += "AND bu.bt_build_id > :after\n";
      paramsBuilder.withInteger("after", filters.getAfterBuildId());
    }
    // we take just above page size to see whether more rows are available but return just rows =
    // pageSize
    sql += "ORDER BY bu.create_date DESC LIMIT :more_than_page_size";
    paramsBuilder.withInteger("more_than_page_size", pageSize + 1);
    List<CompletedBuildSummary> completedBuildsSummary = jdbc.query(sql, paramsBuilder.build(),
        (rs, rowNum) ->
            new CompletedBuildSummary()
                .setBuildId(rs.getInt("bt_build_id"))
                .setBuildName(rs.getString("name"))
                .setFinalStatus(TestStatus.valueOf(rs.getString("final_status")))
                .setError(rs.getString("error"))
                .setBuildSourceType(BuildSourceType.valueOf(rs.getString("source_type")))
                .setCreateDate(
                    DateTimeUtil.sqlUTCTimestampToEpochSecs(rs.getTimestamp("create_date")))
                .setTestTimeMillis(rs.getLong("test_time"))
                .setTotalSuccess(rs.getInt("total_success"))
                .setTotalError(rs.getInt("total_error"))
                .setTotalStopped(rs.getInt("total_stopped"))
                .setTotalAborted(rs.getInt("total_aborted"))
                .setOs(rs.getString("server_os"))
                .setBrowserName(rs.getString("wd_browser_name")));
    int size = completedBuildsSummary.size();
    boolean moreAvailable = size > pageSize;
    Paging paging = new Paging();
    if (size > 0) {
      // we asked for before and we have them this means there is at least 1 newer
      if (filters.getBeforeBuildId() != null) {
        paging.setHasNewer(true);
        paging.setHasOlder(moreAvailable);
      } else if (filters.getAfterBuildId() != null) {
        paging.setHasOlder(true); // similarly, we must have at least 1 older
        paging.setHasNewer(moreAvailable);
      } else {
        // we're showing latest record so no newer
        paging.setHasOlder(moreAvailable);
      }
    }
    return new CompletedBuildsSummaryWithPaging()
        .setCompletedBuildsSummary(completedBuildsSummary.subList(0, Math.min(pageSize, size)))
        .setPaging(paging);
  }
  
  @Override
  public void createAndUpdateVM(BuildVM buildVM, int buildId) {
    transactionTemplate.executeWithoutResult(ts -> {
      int buildVMId = buildVMProvider.newBuildVM(buildVM);
      String sql = "UPDATE bt_build SET bt_build_vm_id = :bt_build_vm_id\n" +
          "WHERE bt_build_id = :bt_build_id";
      CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, new SqlParamsBuilder()
          .withInteger("bt_build_vm_id", buildVMId)
          .withInteger("bt_build_id", buildId).build()));
    });
  }
  
  @Override
  public void updateSession(String sessionId, int buildId) {
    String sql = "UPDATE bt_build SET session_key = :session_key\n" +
        "WHERE bt_build_id = :bt_build_id";
    CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, new SqlParamsBuilder()
        .withVarchar("session_key", sessionId)
        .withInteger("bt_build_id", buildId).build()));
  }
  
  @Override
  public void verifyUsersBuild(int buildId, int userId) {
    common.verifyUsersBuild(buildId, userId);
  }
  
  @Override
  public Optional<RunnerPreferences> getRunnerPrefs(int buildId, int userId) {
    String sql = "SELECT abort_on_failure, aet_keep_single_window,\n" +
        "aet_update_url_blank, aet_reset_timeouts, aet_delete_all_cookies\n" +
        "FROM bt_build AS b\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_build_id = :bt_build_id AND p.zluser_id = :zluser_id";
    List<RunnerPreferences> runnerPreferences = jdbc.query(sql, new SqlParamsBuilder(userId)
        .withInteger("bt_build_id", buildId).build(), (rs, rowNum) ->
        new RunnerPreferences()
            .setAbortOnFailure(rs.getBoolean("abort_on_failure"))
        .setAetKeepSingleWindow(rs.getBoolean("aet_keep_single_window"))
        .setAetUpdateUrlBlank(rs.getBoolean("aet_update_url_blank"))
        .setAetResetTimeouts(rs.getBoolean("aet_reset_timeouts"))
        .setAetDeleteAllCookies(rs.getBoolean("aet_delete_all_cookies")));
    if (runnerPreferences.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(runnerPreferences.get(0));
  }
  
  @Override
  public Optional<CompletedBuildDetails> getCompletedBuildDetails(int buildId, int userId) {
    String sql = "SELECT b.name build_name, b.final_status,\n" +
        "b.create_date AT TIME ZONE 'UTC' AS create_date,\n" +
        "EXTRACT(MILLISECONDS FROM (b.end_date - b.start_date)) AS test_time,\n" +
        "bc.name caps_name, bc.server_os, bc.wd_browser_name, bc.wd_browser_version,\n" +
        "b.server_screen_size, b.server_timezone_with_dst, b.shot_bucket_session_storage,\n" +
        "b.all_done_date AT TIME ZONE 'UTC' AS all_done_date FROM bt_build AS b\n" +
        "INNER JOIN bt_build_captured_capabilities AS bc ON (b.bt_build_id = bc.bt_build_id)\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_build_id = :bt_build_id AND p.zluser_id = :zluser_id\n" +
        "AND b.final_status IS NOT NULL";
    List<CompletedBuildDetails> completedBuildDetailsList = jdbc.query(sql,
        new SqlParamsBuilder(userId).withInteger("bt_build_id", buildId).build(), (rs, rowNum) -> {
          LocalDateTime allDoneDateLocal =
              DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp("all_done_date"));
          Long allDoneDate = allDoneDateLocal != null
              ? DateTimeUtil.utcTimeToEpochSecs(allDoneDateLocal)
              : null;
          return new CompletedBuildDetails()
              .setBuildId(buildId)
              .setBuildName(rs.getString("build_name"))
              .setFinalStatus(TestStatus.valueOf(rs.getString("final_status")))
              .setCreateDate(
                  DateTimeUtil.sqlUTCTimestampToEpochSecs(rs.getTimestamp("create_date")))
              .setTestTimeMillis(rs.getLong("test_time"))
              .setBuildCapsName(rs.getString("caps_name"))
              .setOs(rs.getString("server_os"))
              .setBrowserName(rs.getString("wd_browser_name"))
              .setBrowserVersion(rs.getString("wd_browser_version"))
              .setResolution(rs.getString("server_screen_size"))
              .setTimezone(rs.getString("server_timezone_with_dst"))
              .setShotBucket(rs.getString("shot_bucket_session_storage"))
              .setAllDoneDate(allDoneDate);
        });
    if (completedBuildDetailsList.size() == 0) {
      return Optional.empty();
    }
    CompletedBuildDetails completedBuildDetails = completedBuildDetailsList.get(0);
    // if start or end or both null, we get 0 by using coalesce
    sql = "SELECT bt_test_version_id, bt_test_version_name, bt_file_name, bt_test_name,\n" +
        "status, coalesce(EXTRACT(MILLISECONDS FROM (end_date - start_date)), 0) AS time_taken\n" +
        "FROM bt_build_tests JOIN bt_build_status USING (bt_build_id, bt_test_version_id)\n" +
        "WHERE bt_build_id = :bt_build_id and zluser_id = :zluser_id ORDER BY bt_build_tests_id";
    List<TestVersionDetails> testVersionDetailsList = jdbc.query(sql,
        new SqlParamsBuilder(userId).withInteger("bt_build_id", buildId).build(), (rs, rowNum) ->
            new TestVersionDetails()
                .setVersionId(rs.getInt("bt_test_version_id"))
                .setVersionName(rs.getString("bt_test_version_name"))
                .setStatus(TestStatus.valueOf(rs.getString("status")))
                .setTimeTakenMillis(rs.getLong("time_taken"))
                .setFileName(rs.getString("bt_file_name"))
                .setTestName(rs.getString("bt_test_name")));
    completedBuildDetails.setTestVersionDetailsList(testVersionDetailsList);
    return Optional.of(completedBuildDetails);
  }
  
  @Override
  public String getCapturedCode(int buildId, int versionId, int userId) {
    String sql = "SELECT bt_test_version_code FROM bt_build_tests\n" +
        "JOIN bt_build USING (bt_build_id) JOIN bt_project p USING (bt_project_id)\n" +
        "WHERE bt_build_id = :bt_build_id and bt_test_version_id = :bt_test_version_id\n" +
        "AND p.zluser_id = :zluser_id";
    return jdbc.query(sql,
        new SqlParamsBuilder(userId)
            .withInteger("bt_build_id", buildId)
            .withInteger("bt_test_version_id", versionId).build(),
        CommonUtil.getSingleString()).get(0);
  }
}
