package com.zylitics.front.model;

public class GlobalVars {
  
  private int id;
  
  private String key;
  
  private String value;
  
  public int getId() {
    return id;
  }
  
  public GlobalVars setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getKey() {
    return key;
  }
  
  public GlobalVars setKey(String key) {
    this.key = key;
    return this;
  }
  
  public String getValue() {
    return value;
  }
  
  public GlobalVars setValue(String value) {
    this.value = value;
    return this;
  }
  
  @Override
  public String toString() {
    return "GlobalVars{" +
        "id=" + id +
        ", key='" + key + '\'' +
        ", value='" + value + '\'' +
        '}';
  }
}
