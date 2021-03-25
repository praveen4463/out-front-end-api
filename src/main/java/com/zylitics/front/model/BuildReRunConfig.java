package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
public class BuildReRunConfig {
  
  @NotNull
  private BuildSourceType buildSourceType;
  
  public BuildSourceType getBuildSourceType() {
    return buildSourceType;
  }
  
  public BuildReRunConfig setBuildSourceType(BuildSourceType buildSourceType) {
    this.buildSourceType = buildSourceType;
    return this;
  }
}
