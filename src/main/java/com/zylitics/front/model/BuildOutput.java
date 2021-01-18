package com.zylitics.front.model;

public class BuildOutput {
  
  private String outputsWithLineBreak;
  
  private String nextOutputToken;
  
  public String getOutputsWithLineBreak() {
    return outputsWithLineBreak;
  }
  
  public BuildOutput setOutputsWithLineBreak(String outputsWithLineBreak) {
    this.outputsWithLineBreak = outputsWithLineBreak;
    return this;
  }
  
  public String getNextOutputToken() {
    return nextOutputToken;
  }
  
  public BuildOutput setNextOutputToken(String nextOutputToken) {
    this.nextOutputToken = nextOutputToken;
    return this;
  }
}
