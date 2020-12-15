package com.zylitics.front.model;

import java.util.Objects;

public class UserFromProxy {
  
  private String id;
  
  private String email;
  
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  @Override
  public String toString() {
    return "UserFromProxy{" +
        "id='" + id + '\'' +
        ", email='" + email + '\'' +
        '}';
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserFromProxy that = (UserFromProxy) o;
    return id.equals(that.id) && email.equals(that.email);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(id, email);
  }
}
