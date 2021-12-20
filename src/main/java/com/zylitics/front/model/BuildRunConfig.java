package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Validated
public class BuildRunConfig {
  
  private String buildName;

  @Min(1)
  private int buildCapabilityId;
  
  @NotBlank
  private String displayResolution;
  
  @NotBlank
  private String timezone;
  
  private boolean captureShots;
  
  private boolean captureDriverLogs;
  
  private Map<String, Integer> selectedBuildVarIdPerKey;
  
  private RunnerPreferences runnerPreferences;
  
  @NotNull
  private BuildSourceType buildSourceType;
  
  @NotEmpty
  private List<Integer> versionIds;
  
  public String getBuildName() {
    return buildName;
  }
  
  public BuildRunConfig setBuildName(String buildName) {
    this.buildName = buildName;
    return this;
  }
  
  public int getBuildCapabilityId() {
    return buildCapabilityId;
  }
  
  public BuildRunConfig setBuildCapabilityId(int buildCapabilityId) {
    this.buildCapabilityId = buildCapabilityId;
    return this;
  }
  
  public String getDisplayResolution() {
    return displayResolution;
  }
  
  public BuildRunConfig setDisplayResolution(String displayResolution) {
    this.displayResolution = displayResolution;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public BuildRunConfig setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
  
  public boolean isCaptureShots() {
    return captureShots;
  }
  
  public BuildRunConfig setCaptureShots(boolean captureShots) {
    this.captureShots = captureShots;
    return this;
  }
  
  public boolean isCaptureDriverLogs() {
    return captureDriverLogs;
  }
  
  public BuildRunConfig setCaptureDriverLogs(boolean captureDriverLogs) {
    this.captureDriverLogs = captureDriverLogs;
    return this;
  }
  
  public Map<String, Integer> getSelectedBuildVarIdPerKey() {
    return selectedBuildVarIdPerKey;
  }
  
  public BuildRunConfig setSelectedBuildVarIdPerKey(Map<String, Integer> selectedBuildVarIdPerKey) {
    this.selectedBuildVarIdPerKey = selectedBuildVarIdPerKey;
    return this;
  }
  
  public RunnerPreferences getRunnerPreferences() {
    return runnerPreferences;
  }
  
  public BuildRunConfig setRunnerPreferences(RunnerPreferences runnerPreferences) {
    this.runnerPreferences = runnerPreferences;
    return this;
  }
  
  public BuildSourceType getBuildSourceType() {
    return buildSourceType;
  }
  
  public BuildRunConfig setBuildSourceType(BuildSourceType buildSourceType) {
    this.buildSourceType = buildSourceType;
    return this;
  }
  
  public List<Integer> getVersionIds() {
    return versionIds;
  }
  
  public BuildRunConfig setVersionIds(List<Integer> versionIds) {
    this.versionIds = versionIds;
    return this;
  }
}
