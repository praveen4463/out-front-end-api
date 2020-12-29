package com.zylitics.front.model;

public class Error {
  
  private String message;
  
  @SuppressWarnings("unused")
  public String getMessage() {
    return message;
  }
  
  public Error setMessage(String message) {
    this.message = message;
    return this;
  }
}
