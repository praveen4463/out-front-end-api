package com.zylitics.front.model;

public class NewBuildResponse extends BuildIdentifier {
  
  private String sessionKey;
  
  public String getSessionKey() {
    return sessionKey;
  }
  
  public NewBuildResponse setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
    return this;
  }
}
