package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Validated
public class BuildIdentifier {
  
  @Min(1)
  private int buildId;
  
  @NotBlank
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
