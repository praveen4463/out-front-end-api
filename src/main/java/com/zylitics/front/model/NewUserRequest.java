package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Validated
public class NewUserRequest {
  
  @NotBlank
  private String firstName;
  
  @NotBlank
  private String lastName;
  
  @NotBlank
  private String email;
  
  @Nullable
  @Size(min = 6)
  private String password;
  
  @NotBlank
  private String timezone;
  
  @NotNull
  private Integer utcOffsetInMinutes;
  
  @Nullable
  private Long emailVerificationId;
  
  @Nullable
  private String organizationName;
  
  @Nullable
  private PlanName planName;
  
  private boolean usingLoginProvider;
  
  public String getFirstName() {
    return firstName;
  }
  
  public NewUserRequest setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }
  
  public String getLastName() {
    return lastName;
  }
  
  public NewUserRequest setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
  
  public String getEmail() {
    return email;
  }
  
  public NewUserRequest setEmail(String email) {
    this.email = email;
    return this;
  }
  
  @Nullable
  public String getPassword() {
    return password;
  }
  
  public NewUserRequest setPassword(@Nullable String password) {
    this.password = password;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public NewUserRequest setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
  
  public Integer getUtcOffsetInMinutes() {
    return utcOffsetInMinutes;
  }
  
  public NewUserRequest setUtcOffsetInMinutes(Integer utcOffsetInMinutes) {
    this.utcOffsetInMinutes = utcOffsetInMinutes;
    return this;
  }
  
  @Nullable
  public Long getEmailVerificationId() {
    return emailVerificationId;
  }
  
  public NewUserRequest setEmailVerificationId(@Nullable Long emailVerificationId) {
    this.emailVerificationId = emailVerificationId;
    return this;
  }
  
  @Nullable
  public String getOrganizationName() {
    return organizationName;
  }
  
  public NewUserRequest setOrganizationName(@Nullable String organizationName) {
    this.organizationName = organizationName;
    return this;
  }
  
  @Nullable
  public PlanName getPlanName() {
    return planName;
  }
  
  public NewUserRequest setPlanName(@Nullable PlanName planName) {
    this.planName = planName;
    return this;
  }
  
  public boolean isUsingLoginProvider() {
    return usingLoginProvider;
  }
  
  public NewUserRequest setUsingLoginProvider(boolean usingLoginProvider) {
    this.usingLoginProvider = usingLoginProvider;
    return this;
  }
}
