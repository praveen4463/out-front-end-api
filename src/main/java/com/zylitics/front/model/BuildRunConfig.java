package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Validated
public class BuildRunConfig {

  @Min(1)
  private int buildCapabilityId;
  
  @NotBlank
  private String displayResolution;
  
  @NotBlank
  private String timezone;
  
  private Map<String, Integer> selectedBuildVarIdPerKey;
  
  private RunnerPreferences runnerPreferences;
  
  private BuildSourceType buildSourceType;
  
  @NotEmpty
  private List<Integer> versionIds;
  
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