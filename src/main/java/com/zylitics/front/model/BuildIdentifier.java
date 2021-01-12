package com.zylitics.front.model;

public class BuildIdentifier {
  
  private int buildId;
  
  private String buildKey;
  
  public int getBuildId() {
    return buildId;
  }
  
  public BuildIdentifier setBuildId(int buildId) {
    this.buildId = buildId;
    return this;
  }
  
  public String getBuildKey() {
    return buildKey;
  }
  
  public BuildIdentifier setBuildKey(String buildKey) {
    this.buildKey = buildKey;
    return this;
  }
}
