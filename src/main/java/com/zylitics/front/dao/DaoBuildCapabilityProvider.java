package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.BuildCapability;
import com.zylitics.front.model.BuildCapabilityIdentifier;
import com.zylitics.front.provider.BuildCapabilityProvider;
import com.zylitics.front.util.CollectionUtil;
import com.zylitics.front.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DaoBuildCapabilityProvider extends AbstractDaoProvider implements
    BuildCapabilityProvider {
  
  @Autowired
  DaoBuildCapabilityProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<Integer> saveNewCapability(BuildCapability buildCapability, int userId) {
    Preconditions.checkNotNull(buildCapability, "buildCapability can't be null");
    
    // first check whether there is a duplicate
    String sql = "SELECT count(*) FROM bt_build_capability WHERE zluser_id = :zluser_id" +
        " AND name ILIKE lower(:name);";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(25));
  
    params.put("name", new SqlParameterValue(Types.OTHER, buildCapability.getName()));
  
    params.put("zluser_id", new SqlParameterValue(Types.INTEGER, userId));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    Integer matchingProjects = jdbc.queryForObject(sql, namedParams, Integer.class);
    if (matchingProjects != null && matchingProjects > 0) {
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
  
    addBuildCapsParams(params, buildCapability);
    params.put("create_date", new SqlParameterValue(Types.TIMESTAMP_WITH_TIMEZONE,
        DateTimeUtil.getCurrentUTC()));
  
    namedParams = new MapSqlParameterSource(params);
  
    return Optional.ofNullable(jdbc.queryForObject(sql, namedParams, Integer.class));
  }
  
  @Override
  public int updateCapability(BuildCapability buildCapability, int userId) {
    Preconditions.checkNotNull(buildCapability, "buildCapability can't be null");
    Preconditions.checkArgument(buildCapability.getId() > 0, "buildCapabilityId must be valid");
  
    // first check whether there is a duplicate
    String sql = "SELECT count(*) FROM bt_build_capability WHERE zluser_id = :zluser_id" +
        " AND bt_build_capability_id <> :bt_build_capability_id AND name ILIKE lower(:name);";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(25));
  
    params.put("name", new SqlParameterValue(Types.OTHER, buildCapability.getName()));
  
    params.put("zluser_id", new SqlParameterValue(Types.INTEGER, userId));
  
    params.put("bt_build_capability_id", new SqlParameterValue(Types.INTEGER,
        buildCapability.getId()));
  
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    Integer matchingProjects = jdbc.queryForObject(sql, namedParams, Integer.class);
    if (matchingProjects != null && matchingProjects > 0) {
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
  
    addBuildCapsParams(params, buildCapability);
    
    namedParams = new MapSqlParameterSource(params);
    
    return jdbc.update(sql, namedParams);
  }
  
  @Override
  public List<BuildCapabilityIdentifier> getBuildCapabilitiesIdentifier(int userId) {
    String sql = "SELECT" +
        " bt_build_capability_id" +
        ", name" +
        " FROM bt_build_capability WHERE zluser_id = :zluser_id;";
    
    SqlParameterSource namedParams = new MapSqlParameterSource("zluser_id",
        new SqlParameterValue(Types.INTEGER, userId));
  
    return jdbc.query(sql, namedParams, (rs, rowNum) ->
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
    
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(2));
    params.put("zluser_id", new SqlParameterValue(Types.INTEGER, userId));
    params.put("bt_build_capability_id", new SqlParameterValue(Types.INTEGER, buildCapabilityId));
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    List<BuildCapability> buildCapabilities = jdbc.query(sql, namedParams, (rs, rowNum) ->
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
  public int deleteCapability(int buildCapabilityId, int userId) {
    Preconditions.checkArgument(buildCapabilityId > 0, "buildCapabilityId is required");
    
    String sql = "DELETE FROM bt_build_capability\n" +
        "WHERE bt_build_capability_id = :bt_build_capability_id AND zluser_id = :zluser_id";
  
    Map<String, SqlParameterValue> params = new HashMap<>(CollectionUtil.getInitialCapacity(2));
    params.put("zluser_id", new SqlParameterValue(Types.INTEGER, userId));
    params.put("bt_build_capability_id", new SqlParameterValue(Types.INTEGER, buildCapabilityId));
    SqlParameterSource namedParams = new MapSqlParameterSource(params);
  
    return jdbc.update(sql, namedParams);
  }
  
  private void addBuildCapsParams(Map<String, SqlParameterValue> params,
                                  BuildCapability buildCapability) {
    params.put("server_os", new SqlParameterValue(Types.VARCHAR, buildCapability.getServerOs()));
    params.put("wd_browser_name",
        new SqlParameterValue(Types.VARCHAR, buildCapability.getWdBrowserName()));
    params.put("wd_browser_version",
        new SqlParameterValue(Types.VARCHAR, buildCapability.getWdBrowserVersion()));
    params.put("wd_platform_name",
        new SqlParameterValue(Types.VARCHAR, buildCapability.getWdPlatformName()));
    params.put("wd_accept_insecure_certs",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdAcceptInsecureCerts()));
    params.put("wd_timeouts_script",
        new SqlParameterValue(Types.INTEGER, buildCapability.getWdTimeoutsScript()));
    params.put("wd_timeouts_page_load",
        new SqlParameterValue(Types.INTEGER, buildCapability.getWdTimeoutsPageLoad()));
    params.put("wd_timeouts_element_access",
        new SqlParameterValue(Types.INTEGER, buildCapability.getWdTimeoutsElementAccess()));
    params.put("wd_strict_file_interactability",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdStrictFileInteractability()));
    params.put("wd_unhandled_prompt_behavior",
        new SqlParameterValue(Types.OTHER, buildCapability.getWdUnhandledPromptBehavior()));
    params.put("wd_ie_element_scroll_behavior",
        new SqlParameterValue(Types.VARCHAR, buildCapability.getWdIeElementScrollBehavior()));
    params.put("wd_ie_enable_persistent_hovering",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdIeEnablePersistentHovering()));
    params.put("wd_ie_require_window_focus",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdIeRequireWindowFocus()));
    params.put("wd_ie_disable_native_events",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdIeDisableNativeEvents()));
    params.put("wd_ie_destructively_ensure_clean_session", new SqlParameterValue(Types.BOOLEAN,
        buildCapability.isWdIeDestructivelyEnsureCleanSession()));
    params.put("wd_ie_log_level",
        new SqlParameterValue(Types.VARCHAR, buildCapability.getWdIeLogLevel()));
    params.put("wd_chrome_verbose_logging",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdChromeVerboseLogging()));
    params.put("wd_chrome_silent_output",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdChromeSilentOutput()));
    params.put("wd_chrome_enable_network",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdChromeEnableNetwork()));
    params.put("wd_chrome_enable_page",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdChromeEnablePage()));
    params.put("wd_firefox_log_level",
        new SqlParameterValue(Types.VARCHAR, buildCapability.getWdFirefoxLogLevel()));
    params.put("wd_brw_start_maximize",
        new SqlParameterValue(Types.BOOLEAN, buildCapability.isWdBrwStartMaximize()));
  }
}
