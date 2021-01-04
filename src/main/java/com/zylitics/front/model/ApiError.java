package com.zylitics.front.model;

public class ApiError {
  
  private String message;
  
  @SuppressWarnings("unused")
  public String getMessage() {
    return message;
  }
  
  public ApiError setMessage(String message) {
    this.message = message;
    return this;
  }
}
