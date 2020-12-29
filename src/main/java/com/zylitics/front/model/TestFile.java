package com.zylitics.front.model;

public class TestFile {
  
  private String name;
  
  private String size;
  
  private String createDate;
  
  public String getName() {
    return name;
  }
  
  public TestFile setName(String name) {
    this.name = name;
    return this;
  }
  
  public String getSize() {
    return size;
  }
  
  public TestFile setSize(String size) {
    this.size = size;
    return this;
  }
  
  @SuppressWarnings("unused")
  public String getCreateDate() {
    return createDate;
  }
  
  public TestFile setCreateDate(String createDate) {
    this.createDate = createDate;
    return this;
  }
}
