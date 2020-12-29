package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@Validated
public class Test {
  
  private int id;
  
  @Size(min = 1, max = 200)
  private String name;
  
  @Min(1)
  private int fileId;
  
  private List<TestVersion> versions;
  
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
  
  @SuppressWarnings("unused")
  public List<TestVersion> getVersions() {
    return versions;
  }
  
  public Test setVersions(List<TestVersion> testVersions) {
    this.versions = testVersions;
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
