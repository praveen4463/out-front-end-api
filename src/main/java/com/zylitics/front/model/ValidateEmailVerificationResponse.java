package com.zylitics.front.model;

import javax.annotation.Nullable;

public class ValidateEmailVerificationResponse {
  
  private final long emailVerificationId;
  
  private final String email;
  
  private final EmailVerificationUserType emailVerificationUserType;
  
  @Nullable
  private final String organizationName;
  
  public ValidateEmailVerificationResponse(long emailVerificationId, String email,
                                           EmailVerificationUserType emailVerificationUserType,
                                           @Nullable String organizationName) {
    this.emailVerificationId = emailVerificationId;
    this.email = email;
    this.emailVerificationUserType = emailVerificationUserType;
    this.organizationName = organizationName;
  }
  
  public long getEmailVerificationId() {
    return emailVerificationId;
  }
  
  public String getEmail() {
    return email;
  }
  
  public EmailVerificationUserType getEmailVerificationUserType() {
    return emailVerificationUserType;
  }
  
  @Nullable
  public String getOrganizationName() {
    return organizationName;
  }
}
