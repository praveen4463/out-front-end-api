package com.zylitics.front.model;

import java.util.List;

public class ZwlProgramOutput {
  
  private List<String> outputs;
  
  private String nextOutputToken;
  
  public List<String> getOutputs() {
    return outputs;
  }
  
  public ZwlProgramOutput setOutputs(List<String> outputs) {
    this.outputs = outputs;
    return this;
  }
  
  public String getNextOutputToken() {
    return nextOutputToken;
  }
  
  public ZwlProgramOutput setNextOutputToken(String nextOutputToken) {
    this.nextOutputToken = nextOutputToken;
    return this;
  }
}
