package com.zylitics.front.provider;

import com.zylitics.front.model.EmailVerification;
import com.zylitics.front.model.NewEmailVerification;

import java.util.Optional;

public interface EmailVerificationProvider {

  void newEmailVerification(NewEmailVerification newEmailVerification);
  
  Optional<EmailVerification> getEmailVerification(String code);
  
  Optional<EmailVerification> getEmailVerification(long id);
  
  void updateToUsed(long id);
}
