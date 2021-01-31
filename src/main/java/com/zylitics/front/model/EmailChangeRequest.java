package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
public class EmailChangeRequest {
  
  @NotBlank
  private String newEmail;
  
  public String getNewEmail() {
    return newEmail;
  }
  
  public EmailChangeRequest setNewEmail(String newEmail) {
    this.newEmail = newEmail;
    return this;
  }
}