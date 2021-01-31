package com.zylitics.front.model;

public class NewEmailChange {
  
  private final String previousEmail;
  
  private final String newEmail;
  
  private final String code;
  
  private final int userId;
  
  public NewEmailChange(String previousEmail, String newEmail, String code, int userId) {
    this.previousEmail = previousEmail;
    this.newEmail = newEmail;
    this.code = code;
    this.userId = userId;
  }
  
  public String getPreviousEmail() {
    return previousEmail;
  }
  
  public String getNewEmail() {
    return newEmail;
  }
  
  public String getCode() {
    return code;
  }
  
  public int getUserId() {
    return userId;
  }
}
