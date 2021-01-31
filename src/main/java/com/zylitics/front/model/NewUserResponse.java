package com.zylitics.front.model;

public class NewUserResponse {
  
  private final String shotBucketSessionStorage;
  
  private final int organizationId;
  
  private final String organizationName;
  
  public NewUserResponse(String shotBucketSessionStorage,
                         int organizationId,
                         String organizationName) {
    this.shotBucketSessionStorage = shotBucketSessionStorage;
    this.organizationId = organizationId;
    this.organizationName = organizationName;
  }
  
  public String getShotBucketSessionStorage() {
    return shotBucketSessionStorage;
  }
  
  public int getOrganizationId() {
    return organizationId;
  }
  
  public String getOrganizationName() {
    return organizationName;
  }
}
