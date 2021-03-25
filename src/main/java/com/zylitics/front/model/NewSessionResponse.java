package com.zylitics.front.model;

public class NewSessionResponse {
  
  private BuildIdentifier buildIdentifier;
  
  private String sessionId;
  
  public BuildIdentifier getBuildIdentifier() {
    return buildIdentifier;
  }
  
  public NewSessionResponse setBuildIdentifier(BuildIdentifier buildIdentifier) {
    this.buildIdentifier = buildIdentifier;
    return this;
  }
  
  public String getSessionId() {
    return sessionId;
  }
  
  public NewSessionResponse setSessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }
}
