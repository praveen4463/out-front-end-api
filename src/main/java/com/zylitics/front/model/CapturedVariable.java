package com.zylitics.front.model;

public class CapturedVariable {
  
  private String key;
  
  private String value;
  
  public String getKey() {
    return key;
  }
  
  public CapturedVariable setKey(String key) {
    this.key = key;
    return this;
  }
  
  public String getValue() {
    return value;
  }
  
  public CapturedVariable setValue(String value) {
    this.value = value;
    return this;
  }
}
