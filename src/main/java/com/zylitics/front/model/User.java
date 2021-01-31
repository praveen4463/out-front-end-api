package com.zylitics.front.model;

import javax.annotation.Nullable;

public class User {
  
  private int id;
  
  private String firstName;
  
  private String lastName;
  
  private String email;
  
  private String timezone;
  
  private Role role;
  
  private int organizationId;
  
  private String shotBucketSessionStorage;
  
  private long emailVerificationId;
  
  @Nullable
  private UsersPlan usersPlan;
  
  public int getId() {
    return id;
  }
  
  public User setId(int id) {
    this.id = id;
    return this;
  }
  
  public String getFirstName() {
    return firstName;
  }
  
  public User setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }
  
  public String getLastName() {
    return lastName;
  }
  
  public User setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
  
  public String getEmail() {
    return email;
  }
  
  public User setEmail(String email) {
    this.email = email;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public User setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
  
  public Role getRole() {
    return role;
  }
  
  public User setRole(Role role) {
    this.role = role;
    return this;
  }
  
  public String getShotBucketSessionStorage() {
    return shotBucketSessionStorage;
  }
  
  public User setShotBucketSessionStorage(String shotBucketSessionStorage) {
    this.shotBucketSessionStorage = shotBucketSessionStorage;
    return this;
  }
  
  public int getOrganizationId() {
    return organizationId;
  }
  
  public User setOrganizationId(int organizationId) {
    this.organizationId = organizationId;
    return this;
  }
  
  public long getEmailVerificationId() {
    return emailVerificationId;
  }
  
  public User setEmailVerificationId(long emailVerificationId) {
    this.emailVerificationId = emailVerificationId;
    return this;
  }
  
  @Nullable
  public UsersPlan getUsersPlan() {
    return usersPlan;
  }
  
  public User setUsersPlan(UsersPlan usersPlan) {
    this.usersPlan = usersPlan;
    return this;
  }
}
