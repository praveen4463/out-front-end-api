package com.zylitics.front.model;

import javax.annotation.Nullable;

public class NewEmailVerification {
  
  private final String email;
  
  private final String code;
  
  private final EmailVerificationUserType emailVerificationUserType;
  
  @Nullable
  private final Integer organizationId;
  
  @Nullable
  private final Role role;
  
  public NewEmailVerification(String email, String code,
                              EmailVerificationUserType emailVerificationUserType) {
    this(email, code, emailVerificationUserType, null, null);
  }
  
  public NewEmailVerification(String email, String code,
                              EmailVerificationUserType emailVerificationUserType,
                              @Nullable Integer organizationId, @Nullable Role role) {
    this.email = email;
    this.code = code;
    this.emailVerificationUserType = emailVerificationUserType;
    this.organizationId = organizationId;
    this.role = role;
  }
  
  public String getEmail() {
    return email;
  }
  
  public String getCode() {
    return code;
  }
  
  public EmailVerificationUserType getEmailVerificationUserType() {
    return emailVerificationUserType;
  }
  
  @Nullable
  public Integer getOrganizationId() {
    return organizationId;
  }
  
  @Nullable
  public Role getRole() {
    return role;
  }
}
