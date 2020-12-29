package com.zylitics.front.provider;

import com.zylitics.front.model.BuildCapability;
import com.zylitics.front.model.BuildCapabilityIdentifier;

import java.util.List;
import java.util.Optional;

public interface BuildCapabilityProvider {
  
  int saveNewCapability(BuildCapability buildCapability, int userId);
  
  void updateCapability(BuildCapability buildCapability, int userId);
  
  List<BuildCapabilityIdentifier> getBuildCapabilitiesIdentifier(int userId);
  
  Optional<BuildCapability> getBuildCapability(int buildCapabilityId, int userId);
  
  void deleteCapability(int buildCapabilityId, int userId);
}
