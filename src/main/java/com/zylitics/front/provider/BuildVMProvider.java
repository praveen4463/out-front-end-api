package com.zylitics.front.provider;

import com.zylitics.front.model.BuildVM;

import java.util.Optional;

public interface BuildVMProvider {
  
  int newBuildVM(BuildVM buildVM);
  
  Optional<BuildVM> getBuildVMByBuild(int buildId, int userId);
}
