package com.zylitics.front.provider;

import com.zylitics.front.model.Test;

public interface TestProvider {
  
  Test newTest(Test test, int userId);
  
  void renameTest(Test test, int userId);
  
  void deleteTest(int testId, int userId);
}
