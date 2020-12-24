package com.zylitics.front.model;

public class BuildVars {
  
  private int id;
  
  private String key;
  
  private String value;
  
  private boolean isPrimary;
  
  public int getId() {
    return id;
  }
  
  public BuildVars setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getKey() {
    return key;
  }
  
  public BuildVars setKey(String key) {
    this.key = key;
    return this;
  }
  
  public String getValue() {
    return value;
  }
  
  public BuildVars setValue(String value) {
    this.value = value;
    return this;
  }
  
  public boolean getIsPrimary() {
    return isPrimary;
  }
  
  public BuildVars setIsPrimary(boolean isPrimary) {
    this.isPrimary = isPrimary;
    return this;
  }
  
  @Override
  public String toString() {
    return "BuildVars{" +
        "id=" + id +
        ", key='" + key + '\'' +
        ", value='" + value + '\'' +
        ", isPrimary=" + isPrimary +
        '}';
  }
}
