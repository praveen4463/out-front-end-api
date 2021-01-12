package com.zylitics.front.provider;

import com.zylitics.front.model.User;

import java.util.Optional;

public interface UserProvider {
  
  Optional<User> getUser(int userId);
}
