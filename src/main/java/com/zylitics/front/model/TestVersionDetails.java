package com.zylitics.front.model;

public class TestVersionDetails {

  private int versionId;
  
  private String versionName;
  
  private int totalLines;
  
  private int currentLine;
  
  private TestStatus status;
  
  private long timeTakenMillis;
  
  private String fileName;
  
  private String testName;
  
  public int getVersionId() {
    return versionId;
  }
  
  public TestVersionDetails setVersionId(int versionId) {
    this.versionId = versionId;
    return this;
  }
  
  public String getVersionName() {
    return versionName;
  }
  
  public TestVersionDetails setVersionName(String versionName) {
    this.versionName = versionName;
    return this;
  }
  
  public int getTotalLines() {
    return totalLines;
  }
  
  public TestVersionDetails setTotalLines(int totalLines) {
    this.totalLines = totalLines;
    return this;
  }
  
  public int getCurrentLine() {
    return currentLine;
  }
  
  public TestVersionDetails setCurrentLine(int currentLine) {
    this.currentLine = currentLine;
    return this;
  }
  
  public TestStatus getStatus() {
    return status;
  }
  
  public TestVersionDetails setStatus(TestStatus status) {
    this.status = status;
    return this;
  }
  
  public long getTimeTakenMillis() {
    return timeTakenMillis;
  }
  
  public TestVersionDetails setTimeTakenMillis(long timeTakenMillis) {
    this.timeTakenMillis = timeTakenMillis;
    return this;
  }
  
  public String getFileName() {
    return fileName;
  }
  
  public TestVersionDetails setFileName(String fileName) {
    this.fileName = fileName;
    return this;
  }
  
  public String getTestName() {
    return testName;
  }
  
  public TestVersionDetails setTestName(String testName) {
    this.testName = testName;
    return this;
  }
}
