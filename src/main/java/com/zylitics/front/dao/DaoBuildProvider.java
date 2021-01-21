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
  public Optional<BuildBasicDetails> getBuildBasicDetails(int buildId, int userId) {
    String sql = "SELECT bc.name, bc.server_os, bc.wd_browser_name, bc.wd_browser_version,\n" +
        "b.server_screen_size, b.server_timezone_with_dst, b.shot_bucket_session_storage,\n" +
        "b.all_done_date FROM bt_build AS b\n" +
        "INNER JOIN bt_build_captured_capabilities AS bc ON (b.bt_build_id = bc.bt_build_id)\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_build_id = :bt_build_id AND p.zluser_id = :zluser_id";
    List<BuildBasicDetails> buildBasicDetailsList = jdbc.query(sql,
        new SqlParamsBuilder(userId)
            .withInteger("bt_build_id", buildId).build(), (rs, rowNum) ->
            new BuildBasicDetails()
                .setBuildCapsName(rs.getString("name"))
        .setOs(rs.getString("server_os"))
        .setBrowserName(rs.getString("wd_browser_name"))
        .setBrowserVersion(rs.getString("wd_browser_version"))
        .setResolution(rs.getString("server_screen_size"))
        .setTimezone(rs.getString("server_timezone_with_dst"))
        .setShotBucket(rs.getString("shot_bucket_session_storage"))
        .setAllDoneDate(DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp("all_done_date"))));
    if (buildBasicDetailsList.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(buildBasicDetailsList.get(0));
  }
}
