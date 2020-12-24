package com.zylitics.front.api;

import com.zylitics.front.model.GlobalVars;
import com.zylitics.front.provider.GlobalVarsProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("${app-short-version}/projects/{projectId}/globalVars")
public class GlobalVarsController extends AbstractController {
  
  private final GlobalVarsProvider globalVarsProvider;
  
  public GlobalVarsController(GlobalVarsProvider globalVarsProvider) {
    this.globalVarsProvider = globalVarsProvider;
  }
  
  @GetMapping
  public ResponseEntity<List<GlobalVars>> getGlobalVars(
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return ResponseEntity.ok(globalVarsProvider.getGlobalVars(projectId, getUserId(userInfo)));
  }
}