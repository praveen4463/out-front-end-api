package com.zylitics.front.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class TestVersionRename {
  
  @Size(min = 1, max = 50)
  private String name;
  
  @Min(1)
  private int testId;
  
  public String getName() {
    return name;
  }
  
  public TestVersionRename setName(String name) {
    this.name = name;
    return this;
  }
  
  public int getTestId() {
    return testId;
  }
  
  @SuppressWarnings("unused")
  public TestVersionRename setTestId(int testId) {
    this.testId = testId;
    return this;
  }
}
