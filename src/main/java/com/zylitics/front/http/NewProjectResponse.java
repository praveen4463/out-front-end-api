package com.zylitics.front.http;

public class NewProjectResponse {
  
  private int id;
  
  private String name;
  
  @SuppressWarnings("unused")
  public int getId() {
    return id;
  }
  
  public NewProjectResponse setId(int id) {
    this.id = id;
    return this;
  }
  
  @SuppressWarnings("unused")
  public String getName() {
    return name;
  }
  
  public NewProjectResponse setName(String name) {
    this.name = name;
    return this;
  }
}
