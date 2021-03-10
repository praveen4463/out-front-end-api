package com.zylitics.front.model;

public class CompletedBuildSummary {
  
  private int buildId;
  
  private String buildName;
  
  private TestStatus finalStatus;
  
  private String error;
  
  private BuildSourceType buildSourceType;
  
  private long createDate;
  
  private long testTimeMillis;
  
  private int totalSuccess;
  
  private int totalError;
  
  private int totalStopped;
  
  private int totalAborted;
  
  private String os;
  
  private String browserName;
  
  public int getBuildId() {
    return buildId;
  }
  
  public CompletedBuildSummary setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getBuildName() {
    return buildName;
  }
  
  public CompletedBuildSummary setBuildName(String buildName) {
    this.buildName = buildName;
    return this;
  }
  
  public TestStatus getFinalStatus() {
    return finalStatus;
  }
  
  public CompletedBuildSummary setFinalStatus(TestStatus finalStatus) {
    this.finalStatus = finalStatus;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public CompletedBuildSummary setError(String error) {
    this.error = error;
    return this;
  }
  
  public BuildSourceType getBuildSourceType() {
    return buildSourceType;
  }
  
  public CompletedBuildSummary setBuildSourceType(BuildSourceType buildSourceType) {
    this.buildSourceType = buildSourceType;
    return this;
  }
  
  public long getCreateDate() {
    return createDate;
  }
  
  public CompletedBuildSummary setCreateDate(long createDate) {
    this.createDate = createDate;
    return this;
  }
  
  public long getTestTimeMillis() {
    return testTimeMillis;
  }
  
  public CompletedBuildSummary setTestTimeMillis(long testTimeMillis) {
    this.testTimeMillis = testTimeMillis;
    return this;
  }
  
  public int getTotalSuccess() {
    return totalSuccess;
  }
  
  public CompletedBuildSummary setTotalSuccess(int totalSuccess) {
    this.totalSuccess = totalSuccess;
    return this;
  }
  
  public int getTotalError() {
    return totalError;
  }
  
  public CompletedBuildSummary setTotalError(int totalError) {
    this.totalError = totalError;
    return this;
  }
  
  public int getTotalStopped() {
    return totalStopped;
  }
  
  public CompletedBuildSummary setTotalStopped(int totalStopped) {
    this.totalStopped = totalStopped;
    return this;
  }
  
  public int getTotalAborted() {
    return totalAborted;
  }
  
  public CompletedBuildSummary setTotalAborted(int totalAborted) {
    this.totalAborted = totalAborted;
    return this;
  }
  
  public String getOs() {
    return os;
  }
  
  public CompletedBuildSummary setOs(String os) {
    this.os = os;
    return this;
  }
  
  public String getBrowserName() {
    return browserName;
  }
  
  public CompletedBuildSummary setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }
}
