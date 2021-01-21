package com.zylitics.front.model;

import java.time.LocalDateTime;

public class BuildBasicDetails {
  
  private String os;
  
  private String browserName;
  
  private String browserVersion;
  
  private String resolution;
  
  private String timezone;
  
  private String buildCapsName;
  
  private String shotBucket;
  
  private LocalDateTime allDoneDate;
  
  private boolean driverLogsAvailable;
  
  private boolean perfLogsAvailable;
  
  private boolean elemShotsAvailable;
  
  public String getOs() {
    return os;
  }
  
  public BuildBasicDetails setOs(String os) {
    this.os = os;
    return this;
  }
  
  public String getBrowserName() {
    return browserName;
  }
  
  public BuildBasicDetails setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }
  
  public String getBrowserVersion() {
    return browserVersion;
  }
  
  public BuildBasicDetails setBrowserVersion(String browserVersion) {
    this.browserVersion = browserVersion;
    return this;
  }
  
  public String getResolution() {
    return resolution;
  }
  
  public BuildBasicDetails setResolution(String resolution) {
    this.resolution = resolution;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public BuildBasicDetails setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
  
  public String getBuildCapsName() {
    return buildCapsName;
  }
  
  public BuildBasicDetails setBuildCapsName(String buildCapsName) {
    this.buildCapsName = buildCapsName;
    return this;
  }
  
  public String getShotBucket() {
    return shotBucket;
  }
  
  public BuildBasicDetails setShotBucket(String shotBucket) {
    this.shotBucket = shotBucket;
    return this;
  }
  
  public LocalDateTime getAllDoneDate() {
    return allDoneDate;
  }
  
  public BuildBasicDetails setAllDoneDate(LocalDateTime allDoneDate) {
    this.allDoneDate = allDoneDate;
    return this;
  }
  
  public boolean isDriverLogsAvailable() {
    return driverLogsAvailable;
  }
  
  public BuildBasicDetails setDriverLogsAvailable(boolean driverLogsAvailable) {
    this.driverLogsAvailable = driverLogsAvailable;
    return this;
  }
  
  public boolean isPerfLogsAvailable() {
    return perfLogsAvailable;
  }
  
  public BuildBasicDetails setPerfLogsAvailable(boolean perfLogsAvailable) {
    this.perfLogsAvailable = perfLogsAvailable;
    return this;
  }
  
  public boolean isElemShotsAvailable() {
    return elemShotsAvailable;
  }
  
  public BuildBasicDetails setElemShotsAvailable(boolean elemShotsAvailable) {
    this.elemShotsAvailable = elemShotsAvailable;
    return this;
  }
}
