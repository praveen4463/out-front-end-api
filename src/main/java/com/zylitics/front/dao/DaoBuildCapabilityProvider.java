package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.BuildCapability;
import com.zylitics.front.model.BuildCapabilityIdentifier;
import com.zylitics.front.provider.BuildCapabilityProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DaoBuildCapabilityProvider extends AbstractDaoProvider implements
    BuildCapabilityProvider {
  
  @Autowired
  DaoBuildCapabilityProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public int saveNewCapability(BuildCapability buildCapability, int userId) {
    Preconditions.checkNotNull(buildCapability, "buildCapability can't be null");
    
    // first check whether there is a duplicate
    String sql = "SELECT count(*) FROM bt_build_capability WHERE zluser_id = :zluser_id" +
        " AND name ILIKE lower(:name);";
  
    int matchingProjects = jdbc.query(sql, new SqlParamsBuilder(userId)
        .withOther("name", buildCapability.getName()).build(), CommonUtil.getSingleInt()).get(0);
    if (matchingProjects > 0) {
      throw new IllegalArgumentException("A build capability with the same name already exists");
    }
    
    sql = "INSERT INTO bt_build_capability (name, server_os, wd_browser_name,\n" +
        "wd_browser_version, wd_platform_name, wd_accept_insecure_certs, wd_timeouts_script,\n" +
        "wd_timeouts_page_load, wd_timeouts_element_access, wd_strict_file_interactability,\n" +
        "wd_unhandled_prompt_behavior, wd_ie_element_scroll_behavior,\n" +
        "wd_ie_enable_persistent_hovering, wd_ie_require_window_focus,\n" +
        "wd_ie_disable_native_events, wd_ie_destructively_ensure_clean_session,\n" +
        "wd_ie_log_level, wd_chrome_verbose_logging, wd_chrome_silent_output,\n" +
        "wd_chrome_enable_network, wd_chrome_enable_page, wd_firefox_log_level,\n" +
        "wd_brw_start_maximize, zluser_id, create_date) VALUES\n" +
        "(:name, :server_os, :wd_browser_name, :wd_browser_version,\n" +
        ":wd_platform_name, :wd_accept_insecure_certs, :wd_timeouts_script,\n" +
        ":wd_timeouts_page_load, :wd_timeouts_element_access, :wd_strict_file_interactability,\n" +
        ":wd_unhandled_prompt_behavior, :wd_ie_element_scroll_behavior,\n" +
        ":wd_ie_enable_persistent_hovering, :wd_ie_require_window_focus,\n" +
        ":wd_ie_disable_native_events, :wd_ie_destructively_ensure_clean_session,\n" +
        ":wd_ie_log_level, :wd_chrome_verbose_logging, :wd_chrome_silent_output,\n" +
        ":wd_chrome_enable_network, :wd_chrome_enable_page, :wd_firefox_log_level,\n" +
        ":wd_brw_start_maximize, :zluser_id, :create_date) RETURNING bt_build_capability_id;";
  
    return jdbc.query(sql,
        getBuildCapsParams(buildCapability, userId, true),
        CommonUtil.getSingleInt()).get(0);
  }
  
  @Override
  public void updateCapability(BuildCapability buildCapability, int userId) {
    Preconditions.checkNotNull(buildCapability, "buildCapability can't be null");
    Preconditions.checkArgument(buildCapability.getId() > 0, "buildCapabilityId must be given");
  
    // first check whether there is a duplicate
    String sql = "SELECT count(*) FROM bt_build_capability WHERE zluser_id = :zluser_id" +
        " AND bt_build_capability_id <> :bt_build_capability_id AND name ILIKE lower(:name);";
  
    int matchingProjects = jdbc.query(sql,
        new SqlParamsBuilder(userId)
            .withOther("name", buildCapability.getName())
            .withInteger("bt_build_capability_id", buildCapability.getId()).build(),
        CommonUtil.getSingleInt()).get(0);
    if (matchingProjects > 0) {
      throw new IllegalArgumentException("A build capability with the same name already exists");
    }
  
    sql = "UPDATE bt_build_capability SET name = :name,\n" +
        "server_os = :server_os,\n" +
        "wd_browser_name = :wd_browser_name,\n" +
        "wd_browser_version = :wd_browser_version,\n" +
        "wd_platform_name = :wd_platform_name,\n" +
        "wd_accept_insecure_certs = :wd_accept_insecure_certs,\n" +
        "wd_timeouts_script = :wd_timeouts_script,\n" +
        "wd_timeouts_page_load = :wd_timeouts_page_load,\n" +
        "wd_timeouts_element_access = :wd_timeouts_element_access,\n" +
        "wd_strict_file_interactability = :wd_strict_file_interactability,\n" +
        "wd_unhandled_prompt_behavior = :wd_unhandled_prompt_behavior,\n" +
        "wd_ie_element_scroll_behavior = :wd_ie_element_scroll_behavior,\n" +
        "wd_ie_enable_persistent_hovering = :wd_ie_enable_persistent_hovering,\n" +
        "wd_ie_require_window_focus = :wd_ie_require_window_focus,\n" +
        "wd_ie_disable_native_events = :wd_ie_disable_native_events,\n" +
        "wd_ie_destructively_ensure_clean_session = :wd_ie_destructively_ensure_clean_session,\n" +
        "wd_ie_log_level = :wd_ie_log_level,\n" +
        "wd_chrome_verbose_logging = :wd_chrome_verbose_logging,\n" +
        "wd_chrome_silent_output = :wd_chrome_silent_output,\n" +
        "wd_chrome_enable_network = :wd_chrome_enable_network,\n" +
        "wd_chrome_enable_page = :wd_chrome_enable_page,\n" +
        "wd_firefox_log_level = :wd_firefox_log_level,\n" +
        "wd_brw_start_maximize = :wd_brw_start_maximize\n" +
        "WHERE bt_build_capability_id = :bt_build_capability_id";
  
    int result = jdbc.update(sql, getBuildCapsParams(buildCapability, userId, false));
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public List<BuildCapabilityIdentifier> getBuildCapabilitiesIdentifier(int userId) {
    String sql = "SELECT" +
        " bt_build_capability_id" +
        ", name" +
        " FROM bt_build_capability WHERE zluser_id = :zluser_id;";
  
    return jdbc.query(sql, new SqlParamsBuilder(userId).build(), (rs, rowNum) ->
        new BuildCapabilityIdentifier()
            .setId(rs.getInt("bt_build_capability_id"))
            .setName(rs.getString("name")));
  }
  
  @Override
  public Optional<BuildCapability> getBuildCapability(int buildCapabilityId, int userId) {
    Preconditions.checkArgument(buildCapabilityId > 0, "buildCapabilityId is required");
  
    // when we need create_date, join with user and get timezone, get create_date at that timezone
    // and format the time to display at front end (from util).
    String sql = "SELECT name,\n" +
        "server_os,\n" +
        "wd_browser_name,\n" +
        "wd_browser_version,\n" +
        "wd_platform_name,\n" +
        "wd_accept_insecure_certs,\n" +
        "wd_timeouts_script,\n" +
        "wd_timeouts_page_load,\n" +
        "wd_timeouts_element_access,\n" +
        "wd_strict_file_interactability,\n" +
        "wd_unhandled_prompt_behavior,\n" +
        "wd_ie_element_scroll_behavior,\n" +
        "wd_ie_enable_persistent_hovering,\n" +
        "wd_ie_require_window_focus,\n" +
        "wd_ie_disable_native_events,\n" +
        "wd_ie_destructively_ensure_clean_session,\n" +
        "wd_ie_log_level,\n" +
        "wd_chrome_verbose_logging,\n" +
        "wd_chrome_silent_output,\n" +
        "wd_chrome_enable_network,\n" +
        "wd_chrome_enable_page,\n" +
        "wd_firefox_log_level,\n" +
        "wd_brw_start_maximize\n" +
        "FROM bt_build_capability\n" +
        "WHERE bt_build_capability_id = :bt_build_capability_id AND zluser_id = :zluser_id";
  
    List<BuildCapability> buildCapabilities = jdbc.query(sql,
        new SqlParamsBuilder(userId)
            .withInteger("bt_build_capability_id", buildCapabilityId).build(), (rs, rowNum) ->
        new BuildCapability()
            .setName(rs.getString("name"))
            .setServerOs(rs.getString("server_os"))
            .setWdBrowserName(rs.getString("wd_browser_name"))
            .setWdBrowserVersion(rs.getString("wd_browser_version"))
            .setWdPlatformName(rs.getString("wd_platform_name"))
            .setWdAcceptInsecureCerts(rs.getBoolean("wd_accept_insecure_certs"))
            .setWdTimeoutsScript(rs.getInt("wd_timeouts_script"))
            .setWdTimeoutsPageLoad(rs.getInt("wd_timeouts_page_load"))
            .setWdTimeoutsElementAccess(rs.getInt("wd_timeouts_element_access"))
            .setWdStrictFileInteractability(rs.getBoolean("wd_strict_file_interactability"))
            .setWdUnhandledPromptBehavior(rs.getString("wd_unhandled_prompt_behavior"))
            .setWdIeElementScrollBehavior(rs.getString("wd_ie_element_scroll_behavior"))
            .setWdIeEnablePersistentHovering(rs.getBoolean("wd_ie_enable_persistent_hovering"))
            .setWdIeRequireWindowFocus(rs.getBoolean("wd_ie_require_window_focus"))
            .setWdIeDisableNativeEvents(rs.getBoolean("wd_ie_disable_native_events"))
            .setWdIeDestructivelyEnsureCleanSession(
                rs.getBoolean("wd_ie_destructively_ensure_clean_session"))
            .setWdIeLogLevel(rs.getString("wd_ie_log_level"))
            .setWdChromeVerboseLogging(rs.getBoolean("wd_chrome_verbose_logging"))
            .setWdChromeSilentOutput(rs.getBoolean("wd_chrome_silent_output"))
            .setWdChromeEnableNetwork(rs.getBoolean("wd_chrome_enable_network"))
            .setWdChromeEnablePage(rs.getBoolean("wd_chrome_enable_page"))
            .setWdFirefoxLogLevel(rs.getString("wd_firefox_log_level"))
            .setWdBrwStartMaximize(rs.getBoolean("wd_brw_start_maximize")));
    if (buildCapabilities.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(buildCapabilities.get(0));
  }
  
  @Override
  public void deleteCapability(int buildCapabilityId, int userId) {
    Preconditions.checkArgument(buildCapabilityId > 0, "buildCapabilityId is required");
    
    String sql = "DELETE FROM bt_build_capability\n" +
        "WHERE bt_build_capability_id = :bt_build_capability_id AND zluser_id = :zluser_id";
  
    int result = jdbc.update(sql, new SqlParamsBuilder(userId)
        .withInteger("bt_build_capability_id", buildCapabilityId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  private SqlParameterSource getBuildCapsParams(BuildCapability buildCapability,
                                                int userId,
                                                boolean addCreateDate) {
    SqlParamsBuilder builder = new SqlParamsBuilder(userId);
    if (buildCapability.getId() > 0) {
      builder.withInteger("bt_build_capability_id", buildCapability.getId());
    }
    if (addCreateDate) {
      builder.withCreateDate();
    }
    builder
        .withOther("name", buildCapability.getName())
        .withVarchar("server_os", buildCapability.getServerOs())
        .withVarchar("wd_browser_name", buildCapability.getWdBrowserName())
        .withVarchar("wd_browser_version", buildCapability.getWdBrowserVersion())
        .withVarchar("wd_platform_name", buildCapability.getWdPlatformName())
        .withBoolean("wd_accept_insecure_certs", buildCapability.isWdAcceptInsecureCerts())
        .withInteger("wd_timeouts_script", buildCapability.getWdTimeoutsScript())
        .withInteger("wd_timeouts_page_load", buildCapability.getWdTimeoutsPageLoad())
        .withInteger("wd_timeouts_element_access", buildCapability.getWdTimeoutsElementAccess())
        .withBoolean("wd_strict_file_interactability",
            buildCapability.isWdStrictFileInteractability())
        .withOther("wd_unhandled_prompt_behavior", buildCapability.getWdUnhandledPromptBehavior())
        .withVarchar("wd_ie_element_scroll_behavior",
            buildCapability.getWdIeElementScrollBehavior())
        .withBoolean("wd_ie_enable_persistent_hovering",
            buildCapability.isWdIeEnablePersistentHovering())
        .withBoolean("wd_ie_require_window_focus",
            buildCapability.isWdIeRequireWindowFocus())
        .withBoolean("wd_ie_disable_native_events",
            buildCapability.isWdIeDisableNativeEvents())
        .withBoolean("wd_ie_destructively_ensure_clean_session",
            buildCapability.isWdIeDestructivelyEnsureCleanSession())
        .withVarchar("wd_ie_log_level", buildCapability.getWdIeLogLevel())
        .withBoolean("wd_chrome_verbose_logging", buildCapability.isWdChromeVerboseLogging())
        .withBoolean("wd_chrome_silent_output", buildCapability.isWdChromeSilentOutput())
        .withBoolean("wd_chrome_enable_network", buildCapability.isWdChromeEnableNetwork())
        .withBoolean("wd_chrome_enable_page", buildCapability.isWdChromeEnablePage())
        .withVarchar("wd_firefox_log_level", buildCapability.getWdFirefoxLogLevel())
        .withBoolean("wd_brw_start_maximize", buildCapability.isWdBrwStartMaximize());
    return builder.build();
  }
}
