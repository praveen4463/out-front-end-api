package com.zylitics.front.api;

import com.zylitics.front.model.BuildCapability;
import com.zylitics.front.model.BuildCapabilityIdentifier;
import com.zylitics.front.provider.BuildCapabilityProvider;
import com.zylitics.front.util.CommonUtil;
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
    int userId = getUserId(userInfo);
    
    return ResponseEntity.ok(buildCapabilityProvider.getBuildCapabilitiesIdentifier(userId));
  }
  
  // NOTE: the update is a POST as it accepts updates entire row data, when we have a method that can
  // update specified columns, it should be a PATCH
  @PostMapping
  public ResponseEntity<Integer> newCapabilityOrUpdate(
      @Validated @RequestBody BuildCapability buildCapability,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo)
  {
    int userId = getUserId(userInfo);
    int buildCapabilityId;
    
    if (buildCapability.getId() > 0) {
      int result = buildCapabilityProvider.updateCapability(buildCapability, userId);
      CommonUtil.validateSingleRowDbCommit(result);
      buildCapabilityId = buildCapability.getId();
    } else {
      Optional<Integer> id = buildCapabilityProvider.saveNewCapability(buildCapability, userId);
      if (!id.isPresent()) {
        throw new RuntimeException("Couldn't create build capability " + buildCapability.getName());
      }
      buildCapabilityId = id.get();
    }
    
    return ResponseEntity.ok(buildCapabilityId);
  }
  
  @GetMapping("/{buildCapabilityId}")
  public ResponseEntity<BuildCapability> getBuildCapability(
      @PathVariable int buildCapabilityId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    Optional<BuildCapability> buildCapability =
        buildCapabilityProvider.getBuildCapability(buildCapabilityId, userId);
    if (!buildCapability.isPresent()) {
      throw new RuntimeException("Could get build capability with id " + buildCapabilityId);
    }
    return ResponseEntity.ok(buildCapability.get());
  }
  
  @DeleteMapping("/{buildCapabilityId}")
  public ResponseEntity<Void> deleteBuildCapability(
      @PathVariable int buildCapabilityId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    int result = buildCapabilityProvider.deleteCapability(buildCapabilityId, userId);
    CommonUtil.validateSingleRowDbCommit(result);
    return ResponseEntity.ok().build();
  }
}
