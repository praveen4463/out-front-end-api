package com.zylitics.front.api;

import com.zylitics.front.model.BuildVar;
import com.zylitics.front.provider.BuildVarProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("${app-short-version}/projects/{projectId}/buildVars")
public class BuildVarController extends AbstractController {
  
  private final BuildVarProvider buildVarProvider;
  
  public BuildVarController(BuildVarProvider buildVarProvider) {
    this.buildVarProvider = buildVarProvider;
  }
  
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<Integer> newBuildVar(
      @Validated @RequestBody BuildVar buildVar,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    int id = buildVarProvider.newBuildVar(buildVar, projectId,
        getUserId(userInfo));
    return ResponseEntity.ok(id);
  }
  
  @SuppressWarnings("unused")
  @PatchMapping
  public ResponseEntity<Void> updateBuildVar(
      @RequestBody @Validated UpdateRequest updateRequest,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    buildVarProvider.updateBuildVar(updateRequest.getColumnId(),
        updateRequest.getValue(), updateRequest.getBuildVarId(), projectId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/{buildVarId}")
  public ResponseEntity<Void> deleteBuildVar(
      @PathVariable @Min(1) int buildVarId,
      @RequestParam boolean isPrimary,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    buildVarProvider.deleteBuildVar(buildVarId, isPrimary, projectId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @GetMapping
  public ResponseEntity<List<BuildVar>> getBuildVars(
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return ResponseEntity.ok(buildVarProvider.getBuildVars(projectId, getUserId(userInfo), false));
  }
  
  @Validated
  private static class UpdateRequest {
    
    @NotBlank
    private String columnId;
  
    @NotBlank
    private String value;
  
    @Min(1)
    private int buildVarId;
  
    public String getColumnId() {
      return columnId;
    }
  
    @SuppressWarnings("unused")
    public UpdateRequest setColumnId(String columnId) {
      this.columnId = columnId;
      return this;
    }
  
    public String getValue() {
      return value;
    }
    
    public UpdateRequest setValue(String value) {
      this.value = value;
      return this;
    }
  
    public int getBuildVarId() {
      return buildVarId;
    }
  
    @SuppressWarnings("unused")
    public UpdateRequest setBuildVarId(int buildVarId) {
      this.buildVarId = buildVarId;
      return this;
    }
  }
}
