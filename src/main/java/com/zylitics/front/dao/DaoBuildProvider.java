package com.zylitics.front.dao;

import com.zylitics.front.exception.UnauthorizedException;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.*;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.front.util.DateTimeUtil;
import com.zylitics.front.util.Randoms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class DaoBuildProvider extends AbstractDaoProvider implements BuildProvider {
  
  private static final String BUILD_INSERT_STM =
      "INSERT INTO bt_build\n" +
      "(build_key, name, server_screen_size, server_timezone_with_dst,\n" +
      "shot_bucket_session_storage, abort_on_failure,\n" +
      "capture_shots, capture_driver_logs, notify_on_completion, aet_keep_single_window,\n" +
      "aet_update_url_blank, aet_reset_timeouts, aet_delete_all_cookies, bt_project_id,\n" +
      "source_type, bt_build_request_id, create_date)\n";
  
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
  public int newBuild(BuildRunConfig config,
                      long buildRequestId,
                      User user,
                      int projectId) {
    common.verifyUsersProject(projectId, user);
    Integer newBuildId = transactionTemplate.execute(ts -> newBuildInTransaction(
        config, buildRequestId, user, projectId));
    Objects.requireNonNull(newBuildId);
    return newBuildId;
  }
  
  private String getBuildKey() {
    // TODO: currently I'm generating and putting a random without checking for existence, it may
    //  fail when duplicate. Keep and eye and fix later.
    return randoms.generateRandom(10);
  }
  
  private int newBuildInTransaction(BuildRunConfig config,
                                    long buildRequestId,
                                    User user,
                                    int projectId) {
    String sql = BUILD_INSERT_STM +
        "VALUES (:build_key, :name, :server_screen_size, :server_timezone_with_dst,\n" +
        ":shot_bucket_session_storage, :abort_on_failure,\n" +
        ":capture_shots, :capture_driver_logs, :notify_on_completion, :aet_keep_single_window,\n" +
        ":aet_update_url_blank, :aet_reset_timeouts, :aet_delete_all_cookies, :bt_project_id,\n" +
        ":source_type, :bt_build_request_id, :create_date) RETURNING bt_build_id";
    RunnerPreferences runnerPreferences = config.getRunnerPreferences();
    int buildId = jdbc.query(sql, new SqlParamsBuilder()
        .withVarchar("build_key", getBuildKey())
        .withOther("name", config.getBuildName())
        .withVarchar("server_screen_size", config.getDisplayResolution())
        .withOther("server_timezone_with_dst", config.getTimezone())
        .withVarchar("shot_bucket_session_storage", user.getShotBucketSessionStorage())
        .withBoolean("abort_on_failure", runnerPreferences.isAbortOnFailure())
        .withBoolean("capture_shots", config.isCaptureShots())
        .withBoolean("capture_driver_logs", config.isCaptureDriverLogs())
        .withBoolean("notify_on_completion", config.isNotifyOnCompletion())
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
    
    return buildId;
  }
  
  @Override
  public int duplicateBuild(BuildReRunConfig buildReRunConfig,
                            int buildId,
                            long buildRequestId,
                            User user) {
    common.verifyUsersBuild(buildId, user);
    Integer duplicatedBuildId = transactionTemplate
        .execute(ts -> duplicateBuildInTransaction(buildReRunConfig,buildId, buildRequestId, user));
    Objects.requireNonNull(duplicatedBuildId);
    return duplicatedBuildId;
  }
  
  private int duplicateBuildInTransaction(BuildReRunConfig buildReRunConfig,
                                          int buildId,
                                          long buildRequestId,
                                          User user) {
    // while putting duplicate build, session bucket is taken from what is currently set for user
    // rather than from old build as user may have changed location changing their default bucket.
    String sql = BUILD_INSERT_STM +
        "SELECT :build_key, name, server_screen_size, server_timezone_with_dst,\n" +
        ":shot_bucket_session_storage, abort_on_failure,\n" +
        "capture_shots, capture_driver_logs, notify_on_completion, aet_keep_single_window,\n" +
        "aet_update_url_blank, aet_reset_timeouts, aet_delete_all_cookies, bt_project_id,\n" +
        ":source_type, :bt_build_request_id, :create_date\n" +
        "FROM bt_build WHERE bt_build_id = :bt_build_id \n" +
        "RETURNING bt_build_id";
    int duplicatedBuildId = jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("bt_build_id", buildId)
        .withVarchar("build_key", getBuildKey())
        .withVarchar("shot_bucket_session_storage", user.getShotBucketSessionStorage())
        .withBigint("bt_build_request_id", buildRequestId)
        .withOther("source_type", buildReRunConfig.getBuildSourceType())
        .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
    
    buildCapabilityProvider.duplicateCapturedCapability(duplicatedBuildId, buildId);
    
    testVersionProvider.duplicateCapturedVersions(duplicatedBuildId, buildId);
    
    buildVarProvider.duplicateBuildVars(duplicatedBuildId, buildId);
    
    globalVarProvider.duplicateGlobalVars(duplicatedBuildId, buildId);
    
    return duplicatedBuildId;
  }
  
  @Override
  public Optional<Build> getBuild(int buildId, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT build_key, bu.name, bt_build_vm_id, server_screen_size,\n" +
        "server_timezone_with_dst, session_key,\n" +
        "session_request_start_date AT TIME ZONE 'UTC' AS session_request_start_date,\n" +
        "session_request_end_date AT TIME ZONE 'UTC' AS session_request_end_date,\n" +
        "session_failure_reason,\n" +
        "start_date AT TIME ZONE 'UTC' AS start_date,\n" +
        "end_date AT TIME ZONE 'UTC' AS end_date,\n" +
        "all_done_date AT TIME ZONE 'UTC' AS all_done_date,\n" +
        "final_status, error, shot_bucket_session_storage, abort_on_failure,\n" +
        "capture_shots, capture_driver_logs, notify_on_completion,\n" +
        "aet_keep_single_window, aet_update_url_blank, aet_reset_timeouts,\n" +
        "aet_delete_all_cookies, bt_project_id, source_type, bt_build_request_id,\n" +
        "bu.create_date AT TIME ZONE 'UTC' AS create_date\n" +
        "FROM bt_build bu JOIN bt_project USING (bt_project_id)\n" +
        "WHERE bt_build_id = :bt_build_id AND organization_id = :organization_id";
    List<Build> builds = jdbc.query(sql, new SqlParamsBuilder()
        .withOrganization(user.getOrganizationId())
        .withInteger("bt_build_id", buildId).build(), (rs, rowNum) ->
            new Build()
                .setBuildId(buildId)
                .setBuildKey(rs.getString("build_key"))
                .setName(rs.getString("name"))
                .setBuildVMId(CommonUtil.getIntegerSqlVal(rs, "bt_build_vm_id"))
                .setServerScreenSize(rs.getString("server_screen_size"))
                .setServerTimezone(rs.getString("server_timezone_with_dst"))
                .setSessionKey(rs.getString("session_key"))
                .setSessionRequestStartDate(
                    CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "session_request_start_date"))
                .setSessionRequestEndDate(
                    CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "session_request_end_date"))
                .setSessionFailureReason(CommonUtil.convertEnumFromSqlVal(rs,
                    "session_failure_reason", SessionFailureReason.class))
                .setStartDate(
                    CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "start_date"))
                .setEndDate(
                    CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "end_date"))
                .setAllDoneDate(
                    CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "all_done_date"))
                .setFinalStatus(CommonUtil.convertEnumFromSqlVal(rs,
                    "final_status", TestStatus.class))
                .setError(rs.getString("error"))
                .setShotBucketSessionStorage(rs.getString("shot_bucket_session_storage"))
                .setAbortOnFailure(rs.getBoolean("abort_on_failure"))
                .setCaptureShots(rs.getBoolean("capture_shots"))
                .setCaptureDriverLogs(rs.getBoolean("capture_driver_logs"))
                .setNotifyOnCompletion(rs.getBoolean("notify_on_completion"))
                .setAetKeepSingleWindow(rs.getBoolean("aet_keep_single_window"))
                .setAetUpdateUrlBlank(rs.getBoolean("aet_update_url_blank"))
                .setAetResetTimeouts(rs.getBoolean("aet_reset_timeouts"))
                .setAetDeleteAllCookies(rs.getBoolean("aet_delete_all_cookies"))
                .setProjectId(rs.getInt("bt_project_id"))
                .setSourceType(BuildSourceType.valueOf(rs.getString("source_type")))
                .setBuildRequestId(rs.getLong("bt_build_request_id"))
                .setCreateDate(CommonUtil.getEpochSecsFromSqlTimestamp(rs, "create_date")));
    if (builds.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(builds.get(0));
  }
  
  @Override
  public CompletedBuildsSummaryWithPaging getCompletedBuildsSummaryWithPaging(
      CompletedBuildSummaryFilters filters,
      int pageSize,
      int projectId,
      int userId) {
    User user = common.getUserOwnProps(userId);
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
        "EXTRACT(EPOCH FROM bu.end_date) - EXTRACT(EPOCH FROM bu.start_date) test_time_sec,\n" +
        "total_success, total_error, total_stopped, total_aborted,\n" +
        "server_os, wd_browser_name\n" +
        "FROM bt_build bu JOIN per_status_count USING (bt_build_id)\n" +
        "JOIN bt_build_captured_capabilities USING (bt_build_id)\n" +
        "JOIN bt_project p USING (bt_project_id)\n" +
        "WHERE p.organization_id = :organization_id AND p.bt_project_id = :bt_project_id\n" +
        "AND bu.final_status IS NOT NULL\n" +
        "AND bu.create_date >= :start_date AND bu.create_date <= :end_date\n";
    SqlParamsBuilder paramsBuilder = new SqlParamsBuilder();
    paramsBuilder.withProject(projectId);
    paramsBuilder.withOrganization(user.getOrganizationId());
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
                .setTestTimeMillis(rs.getLong("test_time_sec") * 1000)
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
  public void updateSessionRequestStart(int buildId) {
    String sql = "UPDATE bt_build SET session_request_start_date = :session_request_start_date\n" +
        "WHERE bt_build_id = :bt_build_id";
    CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, new SqlParamsBuilder()
        .withTimestampTimezone("session_request_start_date", DateTimeUtil.getCurrentUTC())
        .withInteger("bt_build_id", buildId).build()));
  }
  
  @Override
  public void updateSession(String sessionId, int buildId) {
    String sql = "UPDATE bt_build SET session_key = :session_key,\n" +
        "session_request_end_date = :session_request_end_date\n" +
        "WHERE bt_build_id = :bt_build_id";
    CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, new SqlParamsBuilder()
        .withVarchar("session_key", sessionId)
        .withTimestampTimezone("session_request_end_date", DateTimeUtil.getCurrentUTC())
        .withInteger("bt_build_id", buildId).build()));
  }
  
  @Override
  public void updateOnSessionFailure(SessionFailureReason sessionFailureReason,
                                           String error,
                                           int buildId) {
    String sql = "UPDATE bt_build SET session_failure_reason = :session_failure_reason,\n" +
        "error = :error, session_request_end_date = :session_request_end_date\n" +
        "WHERE bt_build_id = :bt_build_id";
    CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, new SqlParamsBuilder()
        .withOther("session_failure_reason", sessionFailureReason)
        .withOther("error", error)
        .withTimestampTimezone("session_request_end_date", DateTimeUtil.getCurrentUTC())
        .withInteger("bt_build_id", buildId).build()));
  }
  
  @Override
  public void verifyUsersBuild(int buildId, int userId) {
    common.verifyUsersBuild(buildId, userId);
  }
  
  @Override
  public Optional<RunnerPreferences> getRunnerPrefs(int buildId, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT abort_on_failure, aet_keep_single_window,\n" +
        "aet_update_url_blank, aet_reset_timeouts, aet_delete_all_cookies\n" +
        "FROM bt_build AS b\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_build_id = :bt_build_id AND p.organization_id = :organization_id";
    List<RunnerPreferences> runnerPreferences = jdbc.query(sql, new SqlParamsBuilder()
        .withOrganization(user.getOrganizationId())
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
  
  private List<TestVersionDetails> getTestVersionDetailsList(int buildId) {
    // if start or end or both null, we get 0 by using coalesce
    String sql = "SELECT bt_test_version_id, bt_test_version_name, bt_file_name, bt_test_name,\n" +
        "bt_test_version_code_lines, status, zwl_executing_line,\n" +
        "start_date AT TIME ZONE 'UTC' AS start_date,\n" +
        "end_date AT TIME ZONE 'UTC' AS end_date\n" +
        "FROM bt_build_tests LEFT JOIN bt_build_status USING (bt_build_id, bt_test_version_id)\n" +
        "WHERE bt_build_id = :bt_build_id ORDER BY bt_build_tests_id";
    LocalDateTime current = DateTimeUtil.getCurrentUTCAsLocal();
    return jdbc.query(sql,
        new SqlParamsBuilder().withInteger("bt_build_id", buildId).build(), (rs, rowNum) ->
            new TestVersionDetails()
                .setVersionId(rs.getInt("bt_test_version_id"))
                .setVersionName(rs.getString("bt_test_version_name"))
                .setStatus(CommonUtil.convertEnumFromSqlVal(rs, "status", TestStatus.class))
                .setTotalLines(rs.getInt("bt_test_version_code_lines"))
                .setCurrentLine(rs.getInt("zwl_executing_line"))
                // Time taken calculation: when both are available, its retrieved normally. When
                // both are null (in case build running), we take current instead so that return is
                // 0. When start and non null but end is, we get difference of start and current.
                .setTimeTakenMillis(ChronoUnit.MILLIS.between(
                    DateTimeUtil.fromSqlTimestampIfNullGetGiven(current,
                        rs.getTimestamp("start_date")),
                    DateTimeUtil.fromSqlTimestampIfNullGetGiven(current,
                        rs.getTimestamp("end_date"))))
                .setFileName(rs.getString("bt_file_name"))
                .setTestName(rs.getString("bt_test_name")));
  }
  
  @Override
  public Optional<CompletedBuildDetails> getCompletedBuildDetails(int buildId, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT b.name build_name, b.final_status,\n" +
        "b.create_date AT TIME ZONE 'UTC' AS create_date,\n" +
        "EXTRACT(EPOCH FROM b.end_date) - EXTRACT(EPOCH FROM b.start_date) test_time_sec,\n" +
        "bc.name caps_name, bc.server_os, bc.wd_browser_name, bc.wd_browser_version,\n" +
        "b.server_screen_size, b.server_timezone_with_dst, b.shot_bucket_session_storage,\n" +
        "b.all_done_date AT TIME ZONE 'UTC' AS all_done_date, b.capture_shots\n" +
        "FROM bt_build AS b\n" +
        "INNER JOIN bt_build_captured_capabilities AS bc ON (b.bt_build_id = bc.bt_build_id)\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_build_id = :bt_build_id AND p.organization_id = :organization_id\n" +
        "AND b.final_status IS NOT NULL";
    List<CompletedBuildDetails> completedBuildDetailsList = jdbc.query(sql,
        new SqlParamsBuilder()
            .withOrganization(user.getOrganizationId())
            .withInteger("bt_build_id", buildId).build(), (rs, rowNum) ->
            new CompletedBuildDetails()
                .setBuildId(buildId)
                .setBuildName(rs.getString("build_name"))
                .setFinalStatus(TestStatus.valueOf(rs.getString("final_status")))
                .setCreateDate(
                    DateTimeUtil.sqlUTCTimestampToEpochSecs(rs.getTimestamp("create_date")))
                .setTestTimeMillis(rs.getLong("test_time_sec") * 1000)
                .setBuildCapsName(rs.getString("caps_name"))
                .setOs(rs.getString("server_os"))
                .setBrowserName(rs.getString("wd_browser_name"))
                .setBrowserVersion(rs.getString("wd_browser_version"))
                .setResolution(rs.getString("server_screen_size"))
                .setTimezone(rs.getString("server_timezone_with_dst"))
                .setShotBucket(rs.getString("shot_bucket_session_storage"))
                .setAllDoneDate(CommonUtil.getEpochSecsOrNullFromSqlTimestamp(rs, "all_done_date"))
                .setShotsAvailable(rs.getBoolean("capture_shots")));
    if (completedBuildDetailsList.size() == 0) {
      return Optional.empty();
    }
    CompletedBuildDetails completedBuildDetails = completedBuildDetailsList.get(0);
    completedBuildDetails.setTestVersionDetailsList(getTestVersionDetailsList(buildId));
    return Optional.of(completedBuildDetails);
  }
  
  @Override
  public String getCapturedCode(int buildId, int versionId, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT bt_test_version_code FROM bt_build_tests\n" +
        "JOIN bt_build USING (bt_build_id) JOIN bt_project p USING (bt_project_id)\n" +
        "WHERE bt_build_id = :bt_build_id and bt_test_version_id = :bt_test_version_id\n" +
        "AND p.organization_id = :organization_id";
    return jdbc.query(sql,
        new SqlParamsBuilder()
            .withOrganization(user.getOrganizationId())
            .withInteger("bt_build_id", buildId)
            .withInteger("bt_test_version_id", versionId).build(),
        CommonUtil.getSingleString()).get(0);
  }
  
  @Override
  public List<RunningBuild> getRunningBuilds(Integer after, int projectId, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT bt_build_id, build_key, bu.name, shot_bucket_session_storage,\n" +
        "server_os, capture_shots, wd_browser_name\n" +
        "FROM bt_build bu JOIN bt_build_captured_capabilities USING (bt_build_id)" +
        "JOIN bt_project USING (bt_project_id)\n" +
        "WHERE ((start_date IS NOT NULL AND all_done_date IS NULL)\n" +
        "OR (session_request_start_date IS NOT NULL AND session_request_end_date IS NULL))\n" +
        "AND bt_project_id = :bt_project_id AND organization_id = :organization_id\n";
    SqlParamsBuilder paramsBuilder = new SqlParamsBuilder()
        .withProject(projectId)
        .withOrganization(user.getOrganizationId());
    if (after != null) {
      sql += "AND bt_build_id > :after\n";
      paramsBuilder.withInteger("after", after);
    }
    sql += "ORDER BY bu.create_date DESC";
    return jdbc.query(sql, paramsBuilder.build(), (rs, rowNum) ->
        new RunningBuild()
            .setBuildId(rs.getInt("bt_build_id"))
            .setBuildKey(rs.getString("build_key"))
            .setBuildName(rs.getString("name"))
            .setShotBucket(rs.getString("shot_bucket_session_storage"))
            .setShotsAvailable(rs.getBoolean("capture_shots"))
            .setOs(rs.getString("server_os"))
            .setBrowserName(rs.getString("wd_browser_name")));
  }
  
  @Override
  public RunningBuildSummary getRunningBuildSummary(int buildId, int userId) {
    Build build = getBuild(buildId, userId)
        .orElseThrow(() -> new UnauthorizedException("Invalid buildId or unauthorized user"));
    if (build.getSessionRequestStartDate() == null) {
      throw new IllegalArgumentException("Build " + buildId + " hasn't yet initiated new session");
    }
    // don't validate whether allDoneDate is not null because we will still show fully completed
    // builds on the page for a few seconds to let user learn that it's done.
    RunningBuildSummary runningBuildSummary = new RunningBuildSummary()
        .setBuildId(buildId)
        .setRunningForMillis(ChronoUnit.MILLIS.between(
            DateTimeUtil.epochSecsToUTCLocal(build.getCreateDate()),
            build.getAllDoneDate() != null
                ? DateTimeUtil.epochSecsToUTCLocal(build.getAllDoneDate())
                : DateTimeUtil.getCurrentUTCAsLocal()))
        .setTestVersionDetailsList(getTestVersionDetailsList(buildId));
    if (build.getSessionRequestEndDate() == null) {
      // if we're acquiring session, return as no other detail is needed.
      return runningBuildSummary.setAcquiringSession(true);
    }
    if (build.getSessionKey() == null) {
      if (build.getSessionFailureReason() == null || build.getError() == null) {
        throw new RuntimeException("Build " + buildId + "'s session req is done but there is no" +
            " session key or failure reason");
      }
      // new session is failed, send error
      return runningBuildSummary
          .setNewSessionFail(true)
          .setNewSessionFailureError(build.getError());
    }
    return runningBuildSummary
        .setSessionKey(build.getSessionKey())
        .setAllDone(build.getAllDoneDate() != null)
        .setFinalStatus(build.getFinalStatus());
  }
}
