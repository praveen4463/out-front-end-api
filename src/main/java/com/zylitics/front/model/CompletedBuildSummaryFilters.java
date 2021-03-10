package com.zylitics.front.model;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

public class CompletedBuildSummaryFilters {
  
  private OffsetDateTime startDateUTC;
  
  private OffsetDateTime endDateUTC;
  
  @Nullable
  private TestStatus finalStatus;
  
  @Nullable
  private String browserName;
  
  @Nullable
  private String browserVersion;
  
  @Nullable
  private String os;
  
  @Nullable
  private Integer beforeBuildId;
  
  @Nullable
  private Integer afterBuildId;
  
  public OffsetDateTime getStartDateUTC() {
    return startDateUTC;
  }
  
  public CompletedBuildSummaryFilters setStartDateUTC(OffsetDateTime startDateUTC) {
    this.startDateUTC = startDateUTC;
    return this;
  }
  
  public OffsetDateTime getEndDateUTC() {
    return endDateUTC;
  }
  
  public CompletedBuildSummaryFilters setEndDateUTC(OffsetDateTime endDateUTC) {
    this.endDateUTC = endDateUTC;
    return this;
  }
  
  @Nullable
  public TestStatus getFinalStatus() {
    return finalStatus;
  }
  
  public CompletedBuildSummaryFilters setFinalStatus(@Nullable TestStatus finalStatus) {
    this.finalStatus = finalStatus;
    return this;
  }
  
  @Nullable
  public String getBrowserName() {
    return browserName;
  }
  
  public CompletedBuildSummaryFilters setBrowserName(@Nullable String browserName) {
    this.browserName = browserName;
    return this;
  }
  
  @Nullable
  public String getBrowserVersion() {
    return browserVersion;
  }
  
  public CompletedBuildSummaryFilters setBrowserVersion(@Nullable String browserVersion) {
    this.browserVersion = browserVersion;
    return this;
  }
  
  @Nullable
  public String getOs() {
    return os;
  }
  
  public CompletedBuildSummaryFilters setOs(@Nullable String os) {
    this.os = os;
    return this;
  }
  
  @Nullable
  public Integer getBeforeBuildId() {
    return beforeBuildId;
  }
  
  public CompletedBuildSummaryFilters setBeforeBuildId(@Nullable Integer beforeBuildId) {
    this.beforeBuildId = beforeBuildId;
    return this;
  }
  
  @Nullable
  public Integer getAfterBuildId() {
    return afterBuildId;
  }
  
  public CompletedBuildSummaryFilters setAfterBuildId(@Nullable Integer afterBuildId) {
    this.afterBuildId = afterBuildId;
    return this;
  }
}
