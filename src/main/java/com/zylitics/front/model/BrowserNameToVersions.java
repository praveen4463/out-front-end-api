package com.zylitics.front.model;

public class BrowserNameToVersions {
  
  private String name;
  
  private String[] versions;
  
  public String getName() {
    return name;
  }
  
  public BrowserNameToVersions setName(String name) {
    this.name = name;
    return this;
  }
  
  public String[] getVersions() {
    return versions;
  }
  
  public BrowserNameToVersions setVersions(String[] versions) {
    this.versions = versions;
    return this;
  }
}
