package com.zylitics.front.model;

public class ValidatePasswordResetResponse {
  
  private final long passwordResetId;
  
  private final String email;
  
  public ValidatePasswordResetResponse(long passwordResetId, String email) {
    this.passwordResetId = passwordResetId;
    this.email = email;
  }
  
  public long getPasswordResetId() {
    return passwordResetId;
  }
  
  public String getEmail() {
    return email;
  }
}
