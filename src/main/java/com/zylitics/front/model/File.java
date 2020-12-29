package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Size;
import java.util.List;

@Validated
public class File {
  
  private int id;
  
  @Size(min = 1, max = 50)
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
  
  @SuppressWarnings("unused")
  public List<Test> getTests() {
    return tests;
  }
  
  @SuppressWarnings("UnusedReturnValue")
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
