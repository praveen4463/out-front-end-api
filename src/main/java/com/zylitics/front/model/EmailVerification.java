package com.zylitics.front.model;

import javax.annotation.Nullable;

public class EmailVerification {

  private long id;
  
  private String email;
  
  private String code;
  
  private boolean used;
  
  private EmailVerificationUserType emailVerificationUserType;
  
  @Nullable
  private Role role;
  
  @Nullable
  private Integer organizationId;
  
  public long getId() {
    return id;
  }
  
  public EmailVerification setId(long id) {
    this.id = id;
    return this;
  }
  
  public String getEmail() {
    return email;
  }
  
  public EmailVerification setEmail(String email) {
    this.email = email;
    return this;
  }
  
  public String getCode() {
    return code;
  }
  
  public EmailVerification setCode(String code) {
    this.code = code;
    return this;
  }
  
  public boolean isUsed() {
    return used;
  }
  
  public EmailVerification setUsed(boolean used) {
    this.used = used;
    return this;
  }
  
  public EmailVerificationUserType getEmailVerificationUserType() {
    return emailVerificationUserType;
  }
  
  public EmailVerification setEmailVerificationUserType(EmailVerificationUserType emailVerificationUserType) {
    this.emailVerificationUserType = emailVerificationUserType;
    return this;
  }
  
  @Nullable
  public Role getRole() {
    return role;
  }
  
  public EmailVerification setRole(Role role) {
    this.role = role;
    return this;
  }
  
  @Nullable
  public Integer getOrganizationId() {
    return organizationId;
  }
  
  public EmailVerification setOrganizationId(Integer organizationId) {
    this.organizationId = organizationId;
    return this;
  }
}
