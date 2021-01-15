package com.zylitics.front.services;

import com.google.common.base.Preconditions;
import com.zylitics.front.api.VMService;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.BuildVM;
import com.zylitics.front.model.NewBuildVM;

public class LocalVMService implements VMService {
  
  private final APICoreProperties apiCoreProperties;
  
  public LocalVMService(APICoreProperties apiCoreProperties) {
    this.apiCoreProperties = apiCoreProperties;
  }
  
  @Override
  public BuildVM newBuildVM(NewBuildVM newBuildVM) {
    String localVm = System.getenv(apiCoreProperties.getServices().getLocalVmEnvVar());
    Preconditions.checkNotNull(localVm, "LocalVm env var is not set");
    return new BuildVM().setName("local").setZone("local").setInternalIp(localVm);
  }
}
