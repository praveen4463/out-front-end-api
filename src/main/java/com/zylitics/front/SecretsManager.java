package com.zylitics.front;

import java.io.Closeable;
import java.io.IOException;

public interface SecretsManager extends Closeable {
  
  String getSecretAsPlainText(String secretCloudFileName);
  
  void reAcquireClientAfterClose() throws IOException;
}
