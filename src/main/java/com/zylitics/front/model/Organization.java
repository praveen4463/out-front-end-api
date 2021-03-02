package com.zylitics.front.model;

public class Organization {
  
  private int id;
  
  private String name;
  
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
}
