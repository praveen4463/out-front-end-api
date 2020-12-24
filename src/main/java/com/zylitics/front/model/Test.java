package com.zylitics.front.model;

import java.util.List;

public class Test {
  
  private int id;
  
  private String name;
  
  private int fileId;
  
  private List<Version> versions;
  
  public int getId() {
    return id;
  }
  
  public Test setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getName() {
    return name;
  }
  
  public Test setName(String name) {
    this.name = name;
    return this;
  }
  
  public int getFileId() {
    return fileId;
  }
  
  public Test setFileId(int fileId) {
    this.fileId = fileId;
    return this;
  }
  
  public List<Version> getVersions() {
    return versions;
  }
  
  public Test setVersions(List<Version> versions) {
    this.versions = versions;
    return this;
  }
  
  @Override
  public String toString() {
    return "Test{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", fileId=" + fileId +
        ", versions=" + versions +
        '}';
  }
}
