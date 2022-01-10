package com.zylitics.front.model;

public class EmailPreference {

  private boolean buildSuccess;
  
  private boolean buildFailure;
  
  public boolean isBuildSuccess() {
    return buildSuccess;
  }
  
  public EmailPreference setBuildSuccess(boolean buildSuccess) {
    this.buildSuccess = buildSuccess;
    return this;
  }
  
  public boolean isBuildFailure() {
    return buildFailure;
  }
  
  public EmailPreference setBuildFailure(boolean buildFailure) {
    this.buildFailure = buildFailure;
    return this;
  }
}
