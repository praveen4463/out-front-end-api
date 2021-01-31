package com.zylitics.front.model;

import java.time.LocalDateTime;

public class EmailChange {
  
  private long id;
  
  private String previousEmail;
  
  private String newEmail;
  
  private String code;
  
  private boolean used;
  
  private int userId;
  
  private LocalDateTime createDate;
  
  public long getId() {
    return id;
  }
  
  public EmailChange setId(long id) {
    this.id = id;
    return this;
  }
  
  public String getPreviousEmail() {
    return previousEmail;
  }
  
  public EmailChange setPreviousEmail(String previousEmail) {
    this.previousEmail = previousEmail;
    return this;
  }
  
  public String getNewEmail() {
    return newEmail;
  }
  
  public EmailChange setNewEmail(String newEmail) {
    this.newEmail = newEmail;
    return this;
  }
  
  public String getCode() {
    return code;
  }
  
  public EmailChange setCode(String code) {
    this.code = code;
    return this;
  }
  
  public boolean isUsed() {
    return used;
  }
  
  public EmailChange setUsed(boolean used) {
    this.used = used;
    return this;
  }
  
  public int getUserId() {
    return userId;
  }
  
  public EmailChange setUserId(int userId) {
    this.userId = userId;
    return this;
  }
  
  public LocalDateTime getCreateDate() {
    return createDate;
  }
  
  public EmailChange setCreateDate(LocalDateTime createDate) {
    this.createDate = createDate;
    return this;
  }
}
