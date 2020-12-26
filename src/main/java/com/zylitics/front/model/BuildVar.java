package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Validated
public class BuildVar {
  
  private int id;
  
  @Size(min = 1, max =  100)
  private String key;
  
  @NotBlank
  private String value;
  
  private boolean isPrimary;
  
  public int getId() {
    return id;
  }
  
  public BuildVar setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getKey() {
    return key;
  }
  
  public BuildVar setKey(String key) {
    this.key = key;
    return this;
  }
  
  public String getValue() {
    return value;
  }
  
  public BuildVar setValue(String value) {
    this.value = value;
    return this;
  }
  
  public boolean getIsPrimary() {
    return isPrimary;
  }
  
  public BuildVar setIsPrimary(boolean isPrimary) {
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
