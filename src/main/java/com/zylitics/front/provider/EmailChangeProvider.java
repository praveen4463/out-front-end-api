package com.zylitics.front.provider;

import com.zylitics.front.model.EmailChange;
import com.zylitics.front.model.NewEmailChange;

import java.util.Optional;

public interface EmailChangeProvider {
  
  void newEmailChange(NewEmailChange newEmailChange);
  
  Optional<EmailChange> getEmailChange(String code);
  
  Optional<EmailChange> getEmailChange(long id);
  
  void updateToUsed(long id);
}
