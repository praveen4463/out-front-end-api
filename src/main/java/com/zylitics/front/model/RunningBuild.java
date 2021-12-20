package com.zylitics.front.model;

public class RunningBuild {
  
  private int buildId;
  
  private String buildKey;
  
  private String buildName;
  
  private String shotBucket;
  
  private boolean shotsAvailable;
  
  private String os;
  
  private String browserName;
  
  public int getBuildId() {
    return buildId;
  }
  
  public RunningBuild setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getBuildKey() {
    return buildKey;
  }
  
  public RunningBuild setBuildKey(String buildKey) {
    this.buildKey = buildKey;
    return this;
  }
  
  public String getBuildName() {
    return buildName;
  }
  
  public RunningBuild setBuildName(String buildName) {
    this.buildName = buildName;
    return this;
  }
  
  public String getShotBucket() {
    return shotBucket;
  }
  
  public RunningBuild setShotBucket(String shotBucket) {
    this.shotBucket = shotBucket;
    return this;
  }
  
  public boolean isShotsAvailable() {
    return shotsAvailable;
  }
  
  public RunningBuild setShotsAvailable(boolean shotsAvailable) {
    this.shotsAvailable = shotsAvailable;
    return this;
  }
  
  public String getOs() {
    return os;
  }
  
  public RunningBuild setOs(String os) {
    this.os = os;
    return this;
  }
  
  public String getBrowserName() {
    return browserName;
  }
  
  public RunningBuild setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }
}
