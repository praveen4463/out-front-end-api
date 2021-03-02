package com.zylitics.front.model;

public class EspErrorResponse {
  
  private Error error;
  
  public Error getError() {
    return error;
  }
  
  public EspErrorResponse setError(Error error) {
    this.error = error;
    return this;
  }
  
  public static class Error {
    
    private String message;
  
    public String getMessage() {
      return message;
    }
  
    public Error setMessage(String message) {
      this.message = message;
      return this;
    }
  }
}
