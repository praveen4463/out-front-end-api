package com.zylitics.front.model;

public class NewBuildVM {

  private String os;
  
  private String browserName;
  
  private String browserVersion;
  
  private String displayResolution;
  
  private String timezone;
  
  public String getOs() {
    return os;
  }
  
  public NewBuildVM setOs(String os) {
    this.os = os;
    return this;
  }
  
  public String getBrowserName() {
    return browserName;
  }
  
  public NewBuildVM setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }
  
  public String getBrowserVersion() {
    return browserVersion;
  }
  
  public NewBuildVM setBrowserVersion(String browserVersion) {
    this.browserVersion = browserVersion;
    return this;
  }
  
  public String getDisplayResolution() {
    return displayResolution;
  }
  
  public NewBuildVM setDisplayResolution(String displayResolution) {
    this.displayResolution = displayResolution;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public NewBuildVM setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
}
