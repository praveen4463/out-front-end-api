package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.BuildCapability;
import com.zylitics.front.model.BuildCapabilityIdentifier;
import com.zylitics.front.model.User;
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
  
  private final Common common;
  
  private static final String CAPTURED_CAPS_INSERT_STM =
      "INSERT INTO bt_build_captured_capabilities (bt_build_id, name,\n" +
      "shot_take_test_shot, server_os, wd_browser_name,\n" +
      "wd_browser_version, wd_platform_name, wd_me_device_resolution, wd_accept_insecure_certs,\n" +
      "wd_page_load_strategy, wd_set_window_rect, wd_timeouts_script,\n" +
      "wd_timeouts_page_load, wd_timeouts_element_access, wd_strict_file_interactability,\n" +
      "wd_unhandled_prompt_behavior, wd_ie_element_scroll_behavior,\n" +
      "wd_ie_enable_persistent_hovering, wd_ie_require_window_focus,\n" +
      "wd_ie_disable_native_events, wd_ie_destructively_ensure_clean_session,\n" +
      "wd_ie_log_level, wd_chrome_verbose_logging, wd_chrome_silent_output,\n" +
      "wd_chrome_enable_network, wd_chrome_enable_page, wd_firefox_log_level,\n" +
      "wd_brw_start_maximize)\n";
  
  private static final String CAPTURED_CAPS_SELECT_FIELDS =
      "SELECT :bt_build_id, name,\n" +
      "shot_take_test_shot, server_os, wd_browser_name,\n" +
      "wd_browser_version, wd_platform_name, wd_me_device_resolution, wd_accept_insecure_certs,\n" +
      "wd_page_load_strategy, wd_set_window_rect, wd_timeouts_script,\n" +
      "wd_timeouts_page_load, wd_timeouts_element_access, wd_strict_file_interactability,\n" +
      "wd_unhandled_prompt_behavior, wd_ie_element_scroll_behavior,\n" +
      "wd_ie_enable_persistent_hovering, wd_ie_require_window_focus,\n" +
      "wd_ie_disable_native_events, wd_ie_destructively_ensure_clean_session,\n" +
      "wd_ie_log_level, wd_chrome_verbose_logging, wd_chrome_silent_output,\n" +
      "wd_chrome_enable_network, wd_chrome_enable_page, wd_firefox_log_level,\n" +
      "wd_brw_start_maximize\n";
  
  @Autowired
  DaoBuildCapabilityProvider(NamedParameterJdbcTemplate jdbc, Common common) {
    super(jdbc);
    this.common = common;
  }
  
  @Override
  public int saveNewCapability(BuildCapability buildCapability, int userId) {
    Preconditions.checkNotNull(buildCapability, "buildCapability can't be null");
    User user = common.getUserOwnProps(userId);
    
    // first check whether there is a duplicate
    String sql = "SELECT count(*) FROM bt_build_capability\n" +
        "WHERE organization_id = :organization_id\n" +
        "AND name ILIKE lower(:name);";
  
    int matchingProjects = jdbc.query(sql, new SqlParamsBuilder()
        .withOrganization(user.getOrganizationId())
        .withOther("name", buildCapability.getName()).build(), CommonUtil.getSingleInt()).get(0);
    if (matchingProjects > 0) {
      throw new IllegalArgumentException("A build capability with the same name already exists");
    }
    
    sql = "INSERT INTO bt_build_capability (name, server_os, wd_browser_name,\n" +
        "wd_browser_version, wd_platform_name, wd_me_device_resolution,\n" +
        "wd_accept_insecure_certs, wd_timeouts_script,\n" +
        "wd_timeouts_page_load, wd_timeouts_element_access, wd_strict_file_interactability,\n" +
        "wd_unhandled_prompt_behavior, wd_ie_element_scroll_behavior,\n" +
        "wd_ie_enable_persistent_hovering, wd_ie_require_window_focus,\n" +
        "wd_ie_disable_native_events, wd_ie_destructively_ensure_clean_session,\n" +
        "wd_ie_log_level, wd_chrome_verbose_logging, wd_chrome_silent_output,\n" +
        "wd_chrome_enable_network, wd_chrome_enable_page, wd_firefox_log_level,\n" +
        "wd_brw_start_maximize, zluser_id, organization_id, create_date) VALUES\n" +
        "(:name, :server_os, :wd_browser_name, :wd_browser_version,\n" +
        ":wd_platform_name, :wd_me_device_resolution,\n" +
        ":wd_accept_insecure_certs, :wd_timeouts_script,\n" +
        ":wd_timeouts_page_load, :wd_timeouts_element_access, :wd_strict_file_interactability,\n" +
        ":wd_unhandled_prompt_behavior, :wd_ie_element_scroll_behavior,\n" +
        ":wd_ie_enable_persistent_hovering, :wd_ie_require_window_focus,\n" +
        ":wd_ie_disable_native_events, :wd_ie_destructively_ensure_clean_session,\n" +
        ":wd_ie_log_level, :wd_chrome_verbose_logging, :wd_chrome_silent_output,\n" +
        ":wd_chrome_enable_network, :wd_chrome_enable_page, :wd_firefox_log_level,\n" +
        ":wd_brw_start_maximize, :zluser_id, :organization_id, :create_date)\n" +
        "RETURNING bt_build_capability_id;";
  
    return jdbc.query(sql,
        getBuildCapsParams(buildCapability, userId, user.getOrganizationId(), true),
        CommonUtil.getSingleInt()).get(0);
  }
  
  @Override
  public void captureCapability(int buildCapabilityId, int buildId) {
    String sql = CAPTURED_CAPS_INSERT_STM +
        CAPTURED_CAPS_SELECT_FIELDS +
        "FROM bt_build_capability WHERE bt_build_capability_id = :bt_build_capability_id";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withInteger("bt_build_id", buildId)
        .withInteger("bt_build_capability_id", buildCapabilityId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public void duplicateCapturedCapability(int duplicateBuildId, int originalBuildId) {
    String sql = CAPTURED_CAPS_INSERT_STM +
        CAPTURED_CAPS_SELECT_FIELDS +
        "FROM bt_build_captured_capabilities WHERE bt_build_id = :original_build_id";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withInteger("original_build_id", originalBuildId)
        .withInteger("bt_build_id", duplicateBuildId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public void updateCapability(BuildCapability buildCapability, int userId) {
    Preconditions.checkNotNull(buildCapability, "buildCapability can't be null");
    Preconditions.checkArgument(buildCapability.getId() > 0, "buildCapabilityId must be given");
    
    User user = common.getUserOwnProps(userId);
  
    // first check whether there is a duplicate
    String sql = "SELECT count(*) FROM bt_build_capability\n" +
        "WHERE organization_id = :organization_id\n" +
        "AND bt_build_capability_id <> :bt_build_capability_id AND name ILIKE lower(:name);";
  
    int matchingProjects = jdbc.query(sql,
        new SqlParamsBuilder()
            .withOrganization(user.getOrganizationId())
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
        "wd_me_device_resolution = :wd_me_device_resolution,\n" +
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
  
    int result = jdbc.update(sql, getBuildCapsParams(buildCapability, userId,
        user.getOrganizationId(), false));
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public List<BuildCapabilityIdentifier> getBuildCapabilitiesIdentifier(int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT" +
        " bt_build_capability_id" +
        ", name" +
        " FROM bt_build_capability WHERE organization_id = :organization_id;";
  
    return jdbc.query(sql,
        new SqlParamsBuilder().withOrganization(user.getOrganizationId()).build(),
        (rs, rowNum) -> new BuildCapabilityIdentifier()
            .setId(rs.getInt("bt_build_capability_id"))
            .setName(rs.getString("name")));
  }
  
  @Override
  public Optional<BuildCapability> getBuildCapability(int buildCapabilityId, int userId) {
    Preconditions.checkArgument(buildCapabilityId > 0, "buildCapabilityId is required");
    User user = common.getUserOwnProps(userId);
  
    // when we need create_date, join with user and get timezone, get create_date at that timezone
    // and format the time to display at front end (from util).
    String sql = "SELECT name,\n" +
        "server_os,\n" +
        "wd_browser_name,\n" +
        "wd_browser_version,\n" +
        "wd_platform_name,\n" +
        "wd_me_device_resolution,\n" +
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
        "WHERE bt_build_capability_id = :bt_build_capability_id\n" +
        "AND organization_id = :organization_id";
  
    List<BuildCapability> buildCapabilities = jdbc.query(sql,
        new SqlParamsBuilder()
            .withOrganization(user.getOrganizationId())
            .withInteger("bt_build_capability_id", buildCapabilityId).build(), (rs, rowNum) ->
        new BuildCapability()
            .setName(rs.getString("name"))
            .setServerOs(rs.getString("server_os"))
            .setWdBrowserName(rs.getString("wd_browser_name"))
            .setWdBrowserVersion(rs.getString("wd_browser_version"))
            .setWdPlatformName(rs.getString("wd_platform_name"))
            .setWdMeDeviceResolution(rs.getString("wd_me_device_resolution"))
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
    User user = common.getUserOwnProps(userId);
    
    String sql = "DELETE FROM bt_build_capability\n" +
        "WHERE bt_build_capability_id = :bt_build_capability_id\n" +
        "AND organization_id = :organization_id";
  
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withOrganization(user.getOrganizationId())
        .withInteger("bt_build_capability_id", buildCapabilityId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  private SqlParameterSource getBuildCapsParams(BuildCapability buildCapability,
                                                int userId,
                                                int organizationId,
                                                boolean addCreateDate) {
    SqlParamsBuilder builder = new SqlParamsBuilder(userId);
    builder.withOrganization(organizationId);
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
        .withVarchar("wd_me_device_resolution", buildCapability.getWdMeDeviceResolution())
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
            true) // !! We're always cleaning IE before executing the test, this is important.
        .withVarchar("wd_ie_log_level", buildCapability.getWdIeLogLevel())
        .withBoolean("wd_chrome_verbose_logging", buildCapability.isWdChromeVerboseLogging())
        .withBoolean("wd_chrome_silent_output", buildCapability.isWdChromeSilentOutput())
        .withBoolean("wd_chrome_enable_network", buildCapability.isWdChromeEnableNetwork())
        .withBoolean("wd_chrome_enable_page", buildCapability.isWdChromeEnablePage())
        .withVarchar("wd_firefox_log_level", buildCapability.getWdFirefoxLogLevel())
        .withBoolean("wd_brw_start_maximize", buildCapability.isWdBrwStartMaximize());
    return builder.build();
  }
  
  @Override
  public Optional<BuildCapability> getCapturedCapability(int buildId, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT bc.name, server_os, wd_browser_name,\n" +
        "wd_browser_version, wd_platform_name, wd_me_device_resolution,\n" +
        "wd_accept_insecure_certs, wd_timeouts_script,\n" +
        "wd_timeouts_page_load, wd_timeouts_element_access, wd_strict_file_interactability,\n" +
        "wd_unhandled_prompt_behavior, wd_ie_element_scroll_behavior,\n" +
        "wd_ie_enable_persistent_hovering, wd_ie_require_window_focus,\n" +
        "wd_ie_disable_native_events, wd_ie_destructively_ensure_clean_session,\n" +
        "wd_ie_log_level, wd_chrome_verbose_logging, wd_chrome_silent_output,\n" +
        "wd_chrome_enable_network, wd_chrome_enable_page, wd_firefox_log_level,\n" +
        "wd_brw_start_maximize FROM bt_build_captured_capabilities AS bc\n" +
        "INNER JOIN bt_build AS b ON (bc.bt_build_id = b.bt_build_id)\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE bc.bt_build_id = :bt_build_id AND p.organization_id = :organization_id";
    List<BuildCapability> buildCapabilities = jdbc.query(sql,
        new SqlParamsBuilder()
            .withOrganization(user.getOrganizationId())
            .withInteger("bt_build_id", buildId).build(), (rs, rowNum) ->
            new BuildCapability()
                .setName(rs.getString("name"))
                .setServerOs(rs.getString("server_os"))
                .setWdBrowserName(rs.getString("wd_browser_name"))
                .setWdBrowserVersion(rs.getString("wd_browser_version"))
                .setWdPlatformName(rs.getString("wd_platform_name"))
                .setWdMeDeviceResolution(rs.getString("wd_me_device_resolution"))
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
}
