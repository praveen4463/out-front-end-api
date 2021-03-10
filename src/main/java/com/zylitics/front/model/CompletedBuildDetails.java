package com.zylitics.front.model;

import java.util.List;

public class CompletedBuildDetails {
  
  private int buildId;
  
  private String buildName;
  
  private TestStatus finalStatus;
  
  private long createDate;
  
  private long testTimeMillis;
  
  private String os;
  
  private String browserName;
  
  private String browserVersion;
  
  private String resolution;
  
  private String timezone;
  
  private String buildCapsName;
  
  private String shotBucket;
  
  private Long allDoneDate;
  
  private boolean driverLogsAvailable;
  
  private boolean perfLogsAvailable;
  
  private boolean elemShotsAvailable;
  
  private List<TestVersionDetails> testVersionDetailsList;
  
  public int getBuildId() {
    return buildId;
  }
  
  public CompletedBuildDetails setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getBuildName() {
    return buildName;
  }
  
  public CompletedBuildDetails setBuildName(String buildName) {
    this.buildName = buildName;
    return this;
  }
  
  public TestStatus getFinalStatus() {
    return finalStatus;
  }
  
  public CompletedBuildDetails setFinalStatus(TestStatus finalStatus) {
    this.finalStatus = finalStatus;
    return this;
  }
  
  public long getCreateDate() {
    return createDate;
  }
  
  public CompletedBuildDetails setCreateDate(long createDate) {
    this.createDate = createDate;
    return this;
  }
  
  public long getTestTimeMillis() {
    return testTimeMillis;
  }
  
  public CompletedBuildDetails setTestTimeMillis(long testTimeMillis) {
    this.testTimeMillis = testTimeMillis;
    return this;
  }
  
  public String getOs() {
    return os;
  }
  
  public CompletedBuildDetails setOs(String os) {
    this.os = os;
    return this;
  }
  
  public String getBrowserName() {
    return browserName;
  }
  
  public CompletedBuildDetails setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }
  
  public String getBrowserVersion() {
    return browserVersion;
  }
  
  public CompletedBuildDetails setBrowserVersion(String browserVersion) {
    this.browserVersion = browserVersion;
    return this;
  }
  
  public String getResolution() {
    return resolution;
  }
  
  public CompletedBuildDetails setResolution(String resolution) {
    this.resolution = resolution;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public CompletedBuildDetails setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
  
  public String getBuildCapsName() {
    return buildCapsName;
  }
  
  public CompletedBuildDetails setBuildCapsName(String buildCapsName) {
    this.buildCapsName = buildCapsName;
    return this;
  }
  
  public String getShotBucket() {
    return shotBucket;
  }
  
  public CompletedBuildDetails setShotBucket(String shotBucket) {
    this.shotBucket = shotBucket;
    return this;
  }
  
  public Long getAllDoneDate() {
    return allDoneDate;
  }
  
  public CompletedBuildDetails setAllDoneDate(Long allDoneDate) {
    this.allDoneDate = allDoneDate;
    return this;
  }
  
  public boolean isDriverLogsAvailable() {
    return driverLogsAvailable;
  }
  
  public CompletedBuildDetails setDriverLogsAvailable(boolean driverLogsAvailable) {
    this.driverLogsAvailable = driverLogsAvailable;
    return this;
  }
  
  public boolean isPerfLogsAvailable() {
    return perfLogsAvailable;
  }
  
  public CompletedBuildDetails setPerfLogsAvailable(boolean perfLogsAvailable) {
    this.perfLogsAvailable = perfLogsAvailable;
    return this;
  }
  
  public boolean isElemShotsAvailable() {
    return elemShotsAvailable;
  }
  
  public CompletedBuildDetails setElemShotsAvailable(boolean elemShotsAvailable) {
    this.elemShotsAvailable = elemShotsAvailable;
    return this;
  }
  
  public List<TestVersionDetails> getTestVersionDetailsList() {
    return testVersionDetailsList;
  }
  
  public CompletedBuildDetails setTestVersionDetailsList(
      List<TestVersionDetails> testVersionDetailsList) {
    this.testVersionDetailsList = testVersionDetailsList;
    return this;
  }
}
