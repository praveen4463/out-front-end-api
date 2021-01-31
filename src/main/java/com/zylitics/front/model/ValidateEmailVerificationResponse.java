package com.zylitics.front.model;

import javax.annotation.Nullable;

public class ValidateEmailVerificationResponse {
  
  private final long emailVerificationId;
  
  private final String email;
  
  @Nullable
  private final String organizationName;
  
  public ValidateEmailVerificationResponse(long emailVerificationId, String email) {
    this(emailVerificationId, email, null);
  }
  
  public ValidateEmailVerificationResponse(long emailVerificationId, String email,
                                           @Nullable String organizationName) {
    this.emailVerificationId = emailVerificationId;
    this.email = email;
    this.organizationName = organizationName;
  }
  
  public long getEmailVerificationId() {
    return emailVerificationId;
  }
  
  public String getEmail() {
    return email;
  }
  
  @Nullable
  public String getOrganizationName() {
    return organizationName;
  }
}
