package com.zylitics.front.model;

import javax.annotation.Nullable;

public class NewEmailVerification {
  
  private final String email;
  
  private final String code;
  
  private final boolean used;
  
  private final EmailVerificationUserType emailVerificationUserType;
  
  @Nullable
  private final Integer organizationId;
  
  private final Role role;
  
  public NewEmailVerification(String email, String code,
                              EmailVerificationUserType emailVerificationUserType,
                              Role role, boolean used) {
    this(email, code, emailVerificationUserType, null, role, used);
  }
  
  public NewEmailVerification(String email, String code,
                              EmailVerificationUserType emailVerificationUserType,
                              @Nullable Integer organizationId, Role role, boolean used) {
    this.email = email;
    this.code = code;
    this.emailVerificationUserType = emailVerificationUserType;
    this.organizationId = organizationId;
    this.role = role;
    this.used = used;
  }
  
  public String getEmail() {
    return email;
  }
  
  public String getCode() {
    return code;
  }
  
  public boolean isUsed() {
    return used;
  }
  
  public EmailVerificationUserType getEmailVerificationUserType() {
    return emailVerificationUserType;
  }
  
  @Nullable
  public Integer getOrganizationId() {
    return organizationId;
  }
  
  public Role getRole() {
    return role;
  }
}
