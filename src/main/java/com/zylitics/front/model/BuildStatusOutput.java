package com.zylitics.front.model;

public class BuildStatusOutput {
  
  private TestStatus status;
  
  private int currentLine;
  
  private String output;
  
  private String nextOutputToken;
  
  private long timeTaken;
  
  private RunError error;
  
  public TestStatus getStatus() {
    return status;
  }
  
  public BuildStatusOutput setStatus(TestStatus status) {
    this.status = status;
    return this;
  }
  
  public int getCurrentLine() {
    return currentLine;
  }
  
  public BuildStatusOutput setCurrentLine(int currentLine) {
    this.currentLine = currentLine;
    return this;
  }
  
  public String getOutput() {
    return output;
  }
  
  public BuildStatusOutput setOutput(String output) {
    this.output = output;
    return this;
  }
  
  public String getNextOutputToken() {
    return nextOutputToken;
  }
  
  public BuildStatusOutput setNextOutputToken(String nextOutputToken) {
    this.nextOutputToken = nextOutputToken;
    return this;
  }
  
  public long getTimeTaken() {
    return timeTaken;
  }
  
  public BuildStatusOutput setTimeTaken(long timeTaken) {
    this.timeTaken = timeTaken;
    return this;
  }
  
  public RunError getError() {
    return error;
  }
  
  public BuildStatusOutput setError(RunError error) {
    this.error = error;
    return this;
  }
}
