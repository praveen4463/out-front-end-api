package com.zylitics.front.provider;

import com.zylitics.front.model.NewUser;
import com.zylitics.front.model.User;
import com.zylitics.front.model.UserUpdatableProfile;

import java.util.Optional;

public interface UserProvider {
  
  User newUser(NewUser newUser);
  
  Optional<User> getUser(int userId);
  
  Optional<User> getUser(int userId, boolean ownDetailsOnly);
  
  String getUserEmail(int userId);
  
  Optional<Integer> getUserId(String email);
  
  boolean userWithEmailExist(String email);
  
  void updateEmail(int userId, String newEmail);
  
  void updateProfile(int userId, UserUpdatableProfile userUpdatableProfile);
}
