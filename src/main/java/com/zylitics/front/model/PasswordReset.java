package com.zylitics.front.model;

import java.time.LocalDateTime;

public class PasswordReset {
  
  private long id;
  
  private String code;
  
  private boolean used;
  
  private int userId;
  
  private LocalDateTime createDate;
  
  public long getId() {
    return id;
  }
  
  public PasswordReset setId(long id) {
    this.id = id;
    return this;
  }
  
  public String getCode() {
    return code;
  }
  
  public PasswordReset setCode(String code) {
    this.code = code;
    return this;
  }
  
  public boolean isUsed() {
    return used;
  }
  
  public PasswordReset setUsed(boolean used) {
    this.used = used;
    return this;
  }
  
  public int getUserId() {
    return userId;
  }
  
  public PasswordReset setUserId(int userId) {
    this.userId = userId;
    return this;
  }
  
  public LocalDateTime getCreateDate() {
    return createDate;
  }
  
  public PasswordReset setCreateDate(LocalDateTime createDate) {
    this.createDate = createDate;
    return this;
  }
}
