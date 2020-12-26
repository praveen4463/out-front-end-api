package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Validated
public class GlobalVar {
  
  private int id;
  
  @Size(min = 1, max =  100)
  private String key;
  
  @NotBlank
  private String value;
  
  public int getId() {
    return id;
  }
  
  public GlobalVar setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getKey() {
    return key;
  }
  
  public GlobalVar setKey(String key) {
    this.key = key;
    return this;
  }
  
  public String getValue() {
    return value;
  }
  
  public GlobalVar setValue(String value) {
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
