package com.zylitics.front.model;

import java.util.List;

public class File {
  
  private int id;
  
  private String name;
  
  private List<Test> tests;
  
  public int getId() {
    return id;
  }
  
  public File setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public File setName(String name) {
    this.name = name;
    return this;
  }
  
  public List<Test> getTests() {
    return tests;
  }
  
  public File setTests(List<Test> tests) {
    this.tests = tests;
    return this;
  }
  
  @Override
  public String toString() {
    return "File{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", tests=" + tests +
        '}';
  }
}
