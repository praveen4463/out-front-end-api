package com.zylitics.front.model;

public class NewBuildResponse {
  
  private BuildIdentifier buildIdentifier;
  
  private String sessionId;
  
  public BuildIdentifier getBuildIdentifier() {
    return buildIdentifier;
  }
  
  public NewBuildResponse setBuildIdentifier(BuildIdentifier buildIdentifier) {
    this.buildIdentifier = buildIdentifier;
    return this;
  }
  
  public String getSessionId() {
    return sessionId;
  }
  
  public NewBuildResponse setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }
}
