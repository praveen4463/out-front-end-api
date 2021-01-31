package com.zylitics.front.provider;

import com.zylitics.front.model.NewPasswordReset;
import com.zylitics.front.model.PasswordReset;

import java.util.Optional;

public interface PasswordResetProvider {
  
  void newPasswordReset(NewPasswordReset newPasswordReset);
  
  Optional<PasswordReset> getPasswordReset(String code);
  
  Optional<PasswordReset> getPasswordReset(long id);
  
  void updateToUsed(long id);
}
