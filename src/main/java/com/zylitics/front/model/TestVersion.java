package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Validated
public class TestVersion {
  
  private int id;
  
  @Size(min = 1, max = 50)
  private String name;
  
  @Min(1)
  private int testId;
  
  @Nullable
  private String code;
  
  private boolean isCodeBlank;
  
  private boolean isCurrent;
  
  public int getId() {
    return id;
  }
  
  public TestVersion setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public TestVersion setName(String name) {
    this.name = name;
    return this;
  }
  
  public int getTestId() {
    return testId;
  }
  
  public TestVersion setTestId(int testId) {
    this.testId = testId;
    return this;
  }
  
  @Nullable
  public String getCode() {
    return code;
  }
  
  public TestVersion setCode(String code) {
    this.code = code;
    return this;
  }
  
  public boolean getIsCurrent() {
    return isCurrent;
  }
  
  public TestVersion setIsCurrent(boolean isCurrent) {
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
