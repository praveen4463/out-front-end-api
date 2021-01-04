package com.zylitics.front.model;

public class DryRunResult {
  
  private long timeTaken;
  
  private String output;
  
  private RunError error;
  
  @SuppressWarnings("unused")
  public long getTimeTaken() {
    return timeTaken;
  }
  
  public DryRunResult setTimeTaken(long timeTaken) {
    this.timeTaken = timeTaken;
    return this;
  }
  
  @SuppressWarnings("unused")
  public String getOutput() {
    return output;
  }
  
  @SuppressWarnings("UnusedReturnValue")
  public DryRunResult setOutput(String output) {
    this.output = output;
    return this;
  }
  
  @SuppressWarnings("unused")
  public RunError getError() {
    return error;
  }
  
  public DryRunResult setError(RunError error) {
    this.error = error;
    return this;
  }
}
