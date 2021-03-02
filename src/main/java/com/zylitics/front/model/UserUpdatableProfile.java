package com.zylitics.front.model;

//Notes:
// role is not added as user can't update their own role, only admin can update role of other users
// and that's not an updatable thing from profile
// organizationName must be updatable by admin only
// email update is a separate flow and can't be updated via profile update
public class UserUpdatableProfile {
  
  private String firstName;
  
  private String lastName;
  
  private String timezone;
  
  private String organizationName;
  
  public String getFirstName() {
    return firstName;
  }
  
  public UserUpdatableProfile setFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }
  
  public String getLastName() {
    return lastName;
  }
  
  public UserUpdatableProfile setLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }
  
  public String getTimezone() {
    return timezone;
  }
  
  public UserUpdatableProfile setTimezone(String timezone) {
    this.timezone = timezone;
    return this;
  }
  
  public String getOrganizationName() {
    return organizationName;
  }
  
  public UserUpdatableProfile setOrganizationName(String organizationName) {
    this.organizationName = organizationName;
    return this;
  }
}
