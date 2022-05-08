package com.zylitics.front.model;

import java.util.List;

public class ProjectDownloadableFile {
  
  private String name;
  
  private List<Test> tests;
  
  public String getName() {
    return name;
  }
  
  public ProjectDownloadableFile setName(String name) {
    this.name = name;
    return this;
  }
  
  public List<Test> getTests() {
    return tests;
  }
  
  public ProjectDownloadableFile setTests(List<Test> tests) {
    this.tests = tests;
    return this;
  }
  
  public static class Test {
    private String testName;
  
    private String code;
  
    public String getTestName() {
      return testName;
    }
  
    public Test setTestName(String testName) {
      this.testName = testName;
      return this;
    }
  
    public String getCode() {
      return code;
    }
  
    public Test setCode(String code) {
      this.code = code;
      return this;
    }
  }
}
