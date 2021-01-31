package com.zylitics.front.model;

// When it's a new user not invited by an existing user in their organization, we should create
// new user, new organization and new quota for organization
public class NewUserNewOrganization extends NewUser {
  
  private final PlanName planName;
  
  private final String organizationName;
  
  public NewUserNewOrganization(String firstName, String lastName, String email, String timezone,
                                Role role, String shotBucketSessionStorage,
                                long emailVerificationId, PlanName planName,
                                String organizationName) {
    super(firstName, lastName, email, timezone, role, shotBucketSessionStorage,
        emailVerificationId);
    this.planName = planName;
    this.organizationName = organizationName;
  }
  
  public PlanName getPlanName() {
    return planName;
  }
  
  public String getOrganizationName() {
    return organizationName;
  }
}
