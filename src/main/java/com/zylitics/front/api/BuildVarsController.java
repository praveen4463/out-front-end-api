package com.zylitics.front.api;

import com.zylitics.front.model.BuildVars;
import com.zylitics.front.provider.BuildVarsProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("${app-short-version}/projects/{projectId}/buildVars")
public class BuildVarsController extends AbstractController {
  
  private final BuildVarsProvider buildVarsProvider;
  
  public BuildVarsController(BuildVarsProvider buildVarsProvider) {
    this.buildVarsProvider = buildVarsProvider;
  }
  
  @GetMapping
  public ResponseEntity<List<BuildVars>> getBuildVars(
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return ResponseEntity.ok(buildVarsProvider.getBuildVars(projectId, getUserId(userInfo)));
  }
}
