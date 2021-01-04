package com.zylitics.front.model;

public class VersionParseError {
  
  private int versionId;
  
  private RunError error;
  
  @SuppressWarnings("unused")
  public int getVersionId() {
    return versionId;
  }
  
  public VersionParseError setVersionId(int versionId) {
    this.versionId = versionId;
    return this;
  }
  
  @SuppressWarnings("unused")
  public RunError getError() {
    return error;
  }
  
  public VersionParseError setError(RunError error) {
    this.error = error;
    return this;
  }
}
