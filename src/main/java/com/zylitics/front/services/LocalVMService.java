package com.zylitics.front.services;

import com.zylitics.front.api.VMService;
import com.zylitics.front.model.BuildVM;
import com.zylitics.front.model.NewBuildVM;

public class LocalVMService implements VMService {
  
  private static final String LOCAL_IP = "10.10.1.1";
  
  @Override
  public BuildVM newBuildVM(NewBuildVM newBuildVM) {
    return new BuildVM().setName("local").setZone("local").setInternalIp(LOCAL_IP);
  }
}
