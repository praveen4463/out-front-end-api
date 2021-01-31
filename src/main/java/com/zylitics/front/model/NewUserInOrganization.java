package com.zylitics.front.model;

// When it's an user invited by an existing user in their organization, we should just create
// new user, and link organization. New quota won't be created as such user uses their org's quota
public class NewUserInOrganization extends NewUser {
  
  private final int organizationId;
  
  public NewUserInOrganization(String firstName, String lastName, String email, String timezone,
                               Role role, String shotBucketSessionStorage, long emailVerificationId,
                               int organizationId) {
    super(firstName, lastName, email, timezone, role, shotBucketSessionStorage,
        emailVerificationId);
    this.organizationId = organizationId;
  }
  
  public int getOrganizationId() {
    return organizationId;
  }
}
