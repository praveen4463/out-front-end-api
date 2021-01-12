package com.zylitics.front.api;

import com.zylitics.front.model.BuildVM;
import com.zylitics.front.model.NewBuildVM;

public interface VMService {
  
  BuildVM newBuildVM(NewBuildVM newBuildVM);
}
