package com.zylitics.front.model;

public class BuildOutputDetailsByVersion {
  
  private int buildId;
  
  private int versionId;
  
  private String outputsWithLineBreak;
  
  private String error;
  
  public int getBuildId() {
    return buildId;
  }
  
  public BuildOutputDetailsByVersion setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public int getVersionId() {
    return versionId;
  }
  
  public BuildOutputDetailsByVersion setVersionId(int versionId) {
    this.versionId = versionId;
    return this;
  }
  
  public String getOutputsWithLineBreak() {
    return outputsWithLineBreak;
  }
  
  public BuildOutputDetailsByVersion setOutputsWithLineBreak(String outputsWithLineBreak) {
    this.outputsWithLineBreak = outputsWithLineBreak;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public BuildOutputDetailsByVersion setError(String error) {
    this.error = error;
    return this;
  }
}
