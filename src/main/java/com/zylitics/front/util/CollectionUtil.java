package com.zylitics.front.util;

import com.google.common.base.Preconditions;

public class CollectionUtil {
  
  public static int getInitialCapacity(float loadFactor, int desiredCapacity) {
    Preconditions.checkArgument(desiredCapacity > 0 && loadFactor > 0, "invalid arguments");
    
    float f = desiredCapacity / loadFactor;
    return (int) f + 1; // add one so that upon reaching the limit, collection don't resize
    // and resize only when it crosses limit.
  }
  
  public static int getInitialCapacity(int desiredCapacity) {
    float loadFactor = .75f;
    return getInitialCapacity(loadFactor, desiredCapacity);
  }
}
