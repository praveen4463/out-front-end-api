package com.zylitics.front.model;

public class RunnerPreferences {
  
  private boolean abortOnFailure;
  
  private boolean aetKeepSingleWindow;
  
  private boolean aetUpdateUrlBlank;
  
  private boolean aetResetTimeouts;
  
  private boolean aetDeleteAllCookies;
  
  public boolean isAbortOnFailure() {
    return abortOnFailure;
  }
  
  public RunnerPreferences setAbortOnFailure(boolean abortOnFailure) {
    this.abortOnFailure = abortOnFailure;
    return this;
  }
  
  public boolean isAetKeepSingleWindow() {
    return aetKeepSingleWindow;
  }
  
  public RunnerPreferences setAetKeepSingleWindow(boolean aetKeepSingleWindow) {
    this.aetKeepSingleWindow = aetKeepSingleWindow;
    return this;
  }
  
  public boolean isAetUpdateUrlBlank() {
    return aetUpdateUrlBlank;
  }
  
  public RunnerPreferences setAetUpdateUrlBlank(boolean aetUpdateUrlBlank) {
    this.aetUpdateUrlBlank = aetUpdateUrlBlank;
    return this;
  }
  
  public boolean isAetResetTimeouts() {
    return aetResetTimeouts;
  }
  
  public RunnerPreferences setAetResetTimeouts(boolean aetResetTimeouts) {
    this.aetResetTimeouts = aetResetTimeouts;
    return this;
  }
  
  public boolean isAetDeleteAllCookies() {
    return aetDeleteAllCookies;
  }
  
  public RunnerPreferences setAetDeleteAllCookies(boolean aetDeleteAllCookies) {
    this.aetDeleteAllCookies = aetDeleteAllCookies;
    return this;
  }
}
