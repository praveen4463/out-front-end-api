package com.zylitics.front.model;

public class CompletedVersionStatus {
  
  private TestStatus status;
  
  private String error;
  
  private String urlUponError;
  
  public TestStatus getStatus() {
    return status;
  }
  
  public CompletedVersionStatus setStatus(TestStatus status) {
    this.status = status;
    return this;
  }
  
  public String getError() {
    return error;
  }
  
  public CompletedVersionStatus setError(String error) {
    this.error = error;
    return this;
  }
  
  public String getUrlUponError() {
    return urlUponError;
  }
  
  public CompletedVersionStatus setUrlUponError(String urlUponError) {
    this.urlUponError = urlUponError;
    return this;
  }
}
