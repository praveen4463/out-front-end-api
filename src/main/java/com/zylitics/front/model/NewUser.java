package com.zylitics.front.model;

public abstract class NewUser {
  
  private final String firstName;
  
  private final String lastName;
  
  private final String email;
  
  private final String timezone;
  
  private final Role role;
  
  private final String shotBucketSessionStorage;
  
  private final long emailVerificationId;
  
  public NewUser(String firstName, String lastName, String email, String timezone, Role role,
                 String shotBucketSessionStorage, long emailVerificationId) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.timezone = timezone;
    this.role = role;
    this.shotBucketSessionStorage = shotBucketSessionStorage;
    this.emailVerificationId = emailVerificationId;
  }
  
  public String getFirstName() {
    return firstName;
  }
  
  public String getLastName() {
    return lastName;
  }
  
  public String getEmail() {
    return email;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public Role getRole() {
    return role;
  }
  
  public String getShotBucketSessionStorage() {
    return shotBucketSessionStorage;
  }
  
  public long getEmailVerificationId() {
    return emailVerificationId;
  }
}
