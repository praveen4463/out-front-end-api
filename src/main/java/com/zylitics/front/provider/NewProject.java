package com.zylitics.front.provider;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.time.OffsetDateTime;
import java.util.Objects;

public class NewProject {
  
  private static final int MAX_PROJECT_NAME_LENGTH = 50;
  
  private final String name;
  
  private final int userId;
  
  private final OffsetDateTime createDate;
  
  public NewProject(String name, int userId, OffsetDateTime createDate) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "Project name can't be empty");
    name = name.trim();
    Preconditions.checkArgument(name.length() <= MAX_PROJECT_NAME_LENGTH, "Project name can't" +
        " contain more than " + MAX_PROJECT_NAME_LENGTH + " characters");
    Preconditions.checkArgument(userId > 0, "userId is invalid");
    Preconditions.checkNotNull(createDate, "createDate can't be null");
    
    this.name = name;
    this.userId = userId;
    this.createDate = createDate;
  }
  
  public String getName() {
    return name;
  }
  
  public int getUserId() {
    return userId;
  }
  
  public OffsetDateTime getCreateDate() {
    return createDate;
  }
  
  @Override
  public String toString() {
    return "NewProject{" +
        "name='" + name + '\'' +
        ", userId=" + userId +
        ", createDate=" + createDate +
        '}';
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NewProject that = (NewProject) o;
    return userId == that.userId && name.equals(that.name) && createDate.equals(that.createDate);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(name, userId, createDate);
  }
}
