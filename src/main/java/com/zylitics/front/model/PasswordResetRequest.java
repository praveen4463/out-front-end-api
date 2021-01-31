package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
public class PasswordResetRequest {
  
  @NotBlank
  private String email;
  
  public String getEmail() {
    return email;
  }
  
  public PasswordResetRequest setEmail(String email) {
    this.email = email;
    return this;
  }
}
