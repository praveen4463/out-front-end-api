package com.zylitics.front.http;

public class ErrorResponse {
  
  private String message;
  
  @SuppressWarnings("unused")
  public String getMessage() {
    return message;
  }
  
  public ErrorResponse setMessage(String message) {
    this.message = message;
    return this;
  }
}
