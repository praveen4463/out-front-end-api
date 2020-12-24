package com.zylitics.front.model;

public class BuildCapabilityIdentifier {
  
  private int id;
  
  private String name;
  
  public int getId() {
    return id;
  }
  
  public BuildCapabilityIdentifier setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public BuildCapabilityIdentifier setName(String name) {
    this.name = name;
    return this;
  }
  
  @Override
  public String toString() {
    return "BuildCapabilityIdentifiers{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}
