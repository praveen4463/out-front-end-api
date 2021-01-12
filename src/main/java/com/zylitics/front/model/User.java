package com.zylitics.front.model;

public class User {
  
  private int id;
  
  private String firstName;
  
  private String lastName;
  
  private String email;
  
  private String timezone;
  
  private String role;
  
  private String shotBucketSessionStorage;
  
  private PlanType planType;
  
  private int consumedMinutes;
  
  private int totalParallel;
  
  private int totalMinutes;
  
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
  
  public String getRole() {
    return role;
  }
  
  public User setRole(String role) {
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
  
  public PlanType getPlanType() {
    return planType;
  }
  
  public User setPlanType(PlanType planType) {
    this.planType = planType;
    return this;
  }
  
  public int getConsumedMinutes() {
    return consumedMinutes;
  }
  
  public User setConsumedMinutes(int consumedMinutes) {
    this.consumedMinutes = consumedMinutes;
    return this;
  }
  
  public int getTotalParallel() {
    return totalParallel;
  }
  
  public User setTotalParallel(int totalParallel) {
    this.totalParallel = totalParallel;
    return this;
  }
  
  public int getTotalMinutes() {
    return totalMinutes;
  }
  
  public User setTotalMinutes(int totalMinutes) {
    this.totalMinutes = totalMinutes;
    return this;
  }
}
