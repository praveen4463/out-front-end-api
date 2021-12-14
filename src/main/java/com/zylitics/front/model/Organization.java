package com.zylitics.front.model;

public class Organization {
  
  private int id;
  
  private String name;
  
  private String apiKey;
  
  public int getId() {
    return id;
  }
  
  public Organization setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public Organization setName(String name) {
    this.name = name;
    return this;
  }
  
  public String getApiKey() {
    return apiKey;
  }
  
  public Organization setApiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }
}
