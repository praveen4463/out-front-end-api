package com.zylitics.front.model;

public class FileIdentifier {
  
  private int id;
  
  private String name;
  
  public int getId() {
    return id;
  }
  
  public FileIdentifier setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public FileIdentifier setName(String name) {
    this.name = name;
    return this;
  }
  
  @Override
  public String toString() {
    return "FileIdentifier{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}
