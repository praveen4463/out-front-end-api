package com.zylitics.front.http;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Validated
public class NewProjectRequest {

  @NotBlank
  private String name;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    if (this.name == null) {
      this.name = name;
    }
  }
  
  @Override
  public String toString() {
    return "NewProjectRequest{" +
        "name='" + name + '\'' +
        '}';
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NewProjectRequest that = (NewProjectRequest) o;
    return name.equals(that.name);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
