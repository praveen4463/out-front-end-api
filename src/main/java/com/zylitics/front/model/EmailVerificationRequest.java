package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Validated
public class EmailVerificationRequest {
  
  @NotBlank
  private String email;
  
  @NotNull
  private EmailVerificationUserType emailVerificationUserType;
  
  @Nullable
  private String senderName;
  
  @Nullable
  private Integer organizationId;
  
  @Nullable
  private String organizationName;
  
  @Nullable
  private Role role;
  
  public String getEmail() {
    return email;
  }
  
  public EmailVerificationRequest setEmail(String email) {
    this.email = email;
    return this;
  }
  
  public EmailVerificationUserType getEmailVerificationUserType() {
    return emailVerificationUserType;
  }
  
  public EmailVerificationRequest setEmailVerificationUserType(
      EmailVerificationUserType emailVerificationUserType) {
    this.emailVerificationUserType = emailVerificationUserType;
    return this;
  }
  
  @Nullable
  public String getSenderName() {
    return senderName;
  }
  
  public EmailVerificationRequest setSenderName(@Nullable String senderName) {
    this.senderName = senderName;
    return this;
  }
  
  @Nullable
  public Integer getOrganizationId() {
    return organizationId;
  }
  
  public EmailVerificationRequest setOrganizationId(@Nullable Integer organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  @Nullable
  public String getOrganizationName() {
    return organizationName;
  }
  
  public EmailVerificationRequest setOrganizationName(@Nullable String organizationName) {
    this.organizationName = organizationName;
    return this;
  }
  
  @Nullable
  public Role getRole() {
    return role;
  }
  
  public EmailVerificationRequest setRole(@Nullable Role role) {
    this.role = role;
    return this;
  }
}
