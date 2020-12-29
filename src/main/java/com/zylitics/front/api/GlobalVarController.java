package com.zylitics.front.api;

import com.zylitics.front.model.GlobalVar;
import com.zylitics.front.provider.GlobalVarProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("${app-short-version}/projects/{projectId}/globalVars")
public class GlobalVarController extends AbstractController {
  
  private final GlobalVarProvider globalVarProvider;
  
  public GlobalVarController(GlobalVarProvider globalVarProvider) {
    this.globalVarProvider = globalVarProvider;
  }
  
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<Integer> newGlobalVar(
      @Validated @RequestBody GlobalVar globalVar,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    int id = globalVarProvider.newGlobalVar(globalVar, projectId,
        getUserId(userInfo));
    return ResponseEntity.ok(id);
  }
  
  @SuppressWarnings("unused")
  @PatchMapping
  public ResponseEntity<Void> updateValue(
      @RequestBody @Validated UpdateRequest updateRequest,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    globalVarProvider.updateValue(updateRequest.getValue(),
        updateRequest.getGlobalVarId(), getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/{globalVarId}")
  public ResponseEntity<Void> deleteGlobalVar(
      @PathVariable @Min(1) int globalVarId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    globalVarProvider.deleteGlobalVar(globalVarId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @GetMapping
  public ResponseEntity<List<GlobalVar>> getGlobalVars(
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return ResponseEntity.ok(globalVarProvider.getGlobalVars(projectId, getUserId(userInfo)));
  }
  
  @Validated
  private static class UpdateRequest {
    
    @NotBlank
    private String value;
    
    @Min(1)
    private int globalVarId;
  
    public String getValue() {
      return value;
    }
  
    public UpdateRequest setValue(String value) {
      this.value = value;
      return this;
    }
  
    public int getGlobalVarId() {
      return globalVarId;
    }
  
    @SuppressWarnings("unused")
    public UpdateRequest setGlobalVarId(int globalVarId) {
      this.globalVarId = globalVarId;
      return this;
    }
  }
}