package com.zylitics.front.provider;

import com.zylitics.front.model.NewUser;
import com.zylitics.front.model.User;

import java.util.Optional;

public interface UserProvider {
  
  User newUser(NewUser newUser);
  
  Optional<User> getUserWithPlan(int userId);
  
  String getUserEmail(int userId);
  
  Optional<Integer> getUserId(String email);
  
  boolean userWithEmailExist(String email);
  
  void updateEmail(int userId, String newEmail);
}
