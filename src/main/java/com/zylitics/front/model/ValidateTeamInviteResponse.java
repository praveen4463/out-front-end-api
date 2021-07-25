package com.zylitics.front.model;

public class ValidateTeamInviteResponse {
  
  private final long emailVerificationId;
  
  private final String email;
  
  
  private final String organizationName;
  
  public ValidateTeamInviteResponse(long emailVerificationId,
                                    String email,
                                    String organizationName) {
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
  
  public String getOrganizationName() {
    return organizationName;
  }
}
