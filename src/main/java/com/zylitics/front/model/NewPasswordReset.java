package com.zylitics.front.model;

public class NewPasswordReset {
  
  private final int userId;
  
  private final String code;
  
  public NewPasswordReset(int userId, String code) {
    this.userId = userId;
    this.code = code;
  }
  
  public int getUserId() {
    return userId;
  }
  
  public String getCode() {
    return code;
  }
}
