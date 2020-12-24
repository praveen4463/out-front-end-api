package com.zylitics.front.provider;

import com.zylitics.front.model.BuildCapability;
import com.zylitics.front.model.BuildCapabilityIdentifier;

import java.util.List;
import java.util.Optional;

public interface BuildCapabilityProvider {
  
  Optional<Integer> saveNewCapability(BuildCapability buildCapability, int userId);
  
  int updateCapability(BuildCapability buildCapability, int userId);
  
  List<BuildCapabilityIdentifier> getBuildCapabilitiesIdentifier(int userId);
  
  Optional<BuildCapability> getBuildCapability(int buildCapabilityId, int userId);
  
  int deleteCapability(int buildCapabilityId, int userId);
}
