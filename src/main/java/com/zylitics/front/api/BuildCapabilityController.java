package com.zylitics.front.api;

import com.zylitics.front.model.BuildCapability;
import com.zylitics.front.model.BuildCapabilityIdentifier;
import com.zylitics.front.provider.BuildCapabilityProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app-short-version}/buildCapabilities")
public class BuildCapabilityController extends AbstractController {
  
  private final BuildCapabilityProvider buildCapabilityProvider;
  
  @Autowired
  public BuildCapabilityController(BuildCapabilityProvider buildCapabilityProvider) {
    this.buildCapabilityProvider = buildCapabilityProvider;
  }
  
  @GetMapping
  public ResponseEntity<List<BuildCapabilityIdentifier>> getBuildCapabilitiesIdentifier(
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    return ResponseEntity.ok(buildCapabilityProvider
        .getBuildCapabilitiesIdentifier(getUserId(userInfo)));
  }
  
  // NOTE: the update is a POST as it accepts updates entire row data, when we have a method that can
  // update specified columns, it should be a PATCH
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<Integer> newCapabilityOrUpdate(
      @Validated @RequestBody BuildCapability buildCapability,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo)
  {
    int userId = getUserId(userInfo);
    int buildCapabilityId;
    
    if (buildCapability.getId() > 0) {
      buildCapabilityProvider.updateCapability(buildCapability, userId);
      buildCapabilityId = buildCapability.getId();
    } else {
      buildCapabilityId = buildCapabilityProvider.saveNewCapability(buildCapability, userId);
    }
    
    return ResponseEntity.ok(buildCapabilityId);
  }
  
  @GetMapping("/{buildCapabilityId}")
  public ResponseEntity<BuildCapability> getBuildCapability(
      @PathVariable int buildCapabilityId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    Optional<BuildCapability> buildCapability =
        buildCapabilityProvider.getBuildCapability(buildCapabilityId, getUserId(userInfo));
    if (!buildCapability.isPresent()) {
      throw new RuntimeException("Could get build capability with id " + buildCapabilityId);
    }
    return ResponseEntity.ok(buildCapability.get());
  }
  
  @DeleteMapping("/{buildCapabilityId}")
  public ResponseEntity<Void> deleteBuildCapability(
      @PathVariable int buildCapabilityId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    buildCapabilityProvider.deleteCapability(buildCapabilityId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
}
