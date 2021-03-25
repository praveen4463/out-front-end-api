package com.zylitics.front.model;

import java.util.List;

public class RunningBuildSummary {
  
  private int buildId;
  
  private String sessionKey;
  
  private boolean acquiringSession;
  
  private boolean newSessionFail;
  
  private String newSessionFailureError;
  
  private boolean allDone;
  
  private long runningForMillis;
  
  private TestStatus finalStatus;
  
  private List<TestVersionDetails> testVersionDetailsList;
  
  public int getBuildId() {
    return buildId;
  }
  
  public RunningBuildSummary setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getSessionKey() {
    return sessionKey;
  }
  
  public RunningBuildSummary setSessionKey(String sessionKey) {
    this.sessionKey = sessionKey;
    return this;
  }
  
  public boolean isAcquiringSession() {
    return acquiringSession;
  }
  
  public RunningBuildSummary setAcquiringSession(boolean acquiringSession) {
    this.acquiringSession = acquiringSession;
    return this;
  }
  
  public boolean isNewSessionFail() {
    return newSessionFail;
  }
  
  public RunningBuildSummary setNewSessionFail(boolean newSessionFail) {
    this.newSessionFail = newSessionFail;
    return this;
  }
  
  public String getNewSessionFailureError() {
    return newSessionFailureError;
  }
  
  public RunningBuildSummary setNewSessionFailureError(String newSessionFailureError) {
    this.newSessionFailureError = newSessionFailureError;
    return this;
  }
  
  public boolean isAllDone() {
    return allDone;
  }
  
  public RunningBuildSummary setAllDone(boolean allDone) {
    this.allDone = allDone;
    return this;
  }
  
  public long getRunningForMillis() {
    return runningForMillis;
  }
  
  public RunningBuildSummary setRunningForMillis(long runningForMillis) {
    this.runningForMillis = runningForMillis;
    return this;
  }
  
  public TestStatus getFinalStatus() {
    return finalStatus;
  }
  
  public RunningBuildSummary setFinalStatus(TestStatus finalStatus) {
    this.finalStatus = finalStatus;
    return this;
  }
  
  public List<TestVersionDetails> getTestVersionDetailsList() {
    return testVersionDetailsList;
  }
  
  public RunningBuildSummary setTestVersionDetailsList(List<TestVersionDetails> testVersionDetailsList) {
    this.testVersionDetailsList = testVersionDetailsList;
    return this;
  }
}
