package com.zylitics.front.model;

import javax.annotation.Nullable;

public class Version {
  
  private int id;
  
  private String name;
  
  private int testId;
  
  @Nullable
  private String code;
  
  private boolean isCurrent;
  
  public int getId() {
    return id;
  }
  
  public Version setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public Version setName(String name) {
    this.name = name;
    return this;
  }
  
  public int getTestId() {
    return testId;
  }
  
  public Version setTestId(int testId) {
    this.testId = testId;
    return this;
  }
  
  @Nullable
  public String getCode() {
    return code;
  }
  
  public Version setCode(String code) {
    this.code = code;
    return this;
  }
  
  public boolean getIsCurrent() {
    return isCurrent;
  }
  
  public Version setIsCurrent(boolean isCurrent) {
    this.isCurrent = isCurrent;
    return this;
  }
  
  @Override
  public String toString() {
    return "Version{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", testId=" + testId +
        ", code='" + code + '\'' +
        ", isCurrent=" + isCurrent +
        '}';
  }
}
