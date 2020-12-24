package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
public class Project {

  private int id;
  
  @NotBlank
  private String name;
  
  public int getId() {
    return id;
  }
  
  public Project setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public Project setName(String name) {
    this.name = name;
    return this;
  }
  
  @Override
  public String toString() {
    return "Project{" +
        "id=" + id +
        ", name='" + name + '\'' +
        '}';
  }
}
