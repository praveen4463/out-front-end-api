package com.zylitics.front.api;

import com.zylitics.front.model.*;
import com.zylitics.front.provider.BuildVarProvider;
import com.zylitics.front.provider.GlobalVarProvider;
import com.zylitics.front.provider.TestVersionProvider;
import com.zylitics.front.runs.DryRun;
import com.zylitics.front.runs.ParseRun;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.zwl.api.ZwlDryRunProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${app-short-version}")
public class TestVersionController extends AbstractController {
  
  private final TestVersionProvider testVersionProvider;
  
  private final BuildVarProvider buildVarProvider;
  
  private final GlobalVarProvider globalVarProvider;
  
  private final ParseRun parseRun;
  
  public TestVersionController(TestVersionProvider testVersionProvider,
                               ParseRun parseRun,
                               BuildVarProvider buildVarProvider,
                               GlobalVarProvider globalVarProvider) {
    this.testVersionProvider = testVersionProvider;
    this.parseRun = parseRun;
    this.buildVarProvider = buildVarProvider;
    this.globalVarProvider = globalVarProvider;
  }
  
  @SuppressWarnings("unused")
  @PostMapping("/versions")
  public ResponseEntity<TestVersion> newVersion(
      @Validated @RequestBody TestVersion testVersion,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return ResponseEntity.ok(testVersionProvider.newVersion(testVersion, getUserId(userInfo)));
  }
  
  @SuppressWarnings("unused")
  @PatchMapping("/versions/{versionId}/renameVersion")
  public ResponseEntity<Void> renameVersion(
      @Validated @RequestBody TestVersionRename testVersionRename,
      @PathVariable @Min(1) int versionId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    testVersionProvider.renameVersion(versionId, testVersionRename, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @SuppressWarnings("unused")
  @PatchMapping("/versions/{versionId}/updateCodeAndParse")
  public ResponseEntity<VersionParseError> updateCodeAndParse(
      @Validated @RequestBody UpdateCodeRequest updateCodeRequest,
      @PathVariable @Min(1) int versionId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    int userId = getUserId(userInfo);
    String code = updateCodeRequest.getCode();
    String existingCode = testVersionProvider.getCode(versionId, userId);
    if (!existingCode.equals(code)) {
      // save it
      testVersionProvider.updateCode(versionId, code, userId);
    }
    // even if the code is same, parse the code. Sometimes when client gets error on calling this api,
    // it's not clear whether save failed or parsing, client just doesn't save code locally tells user
    // that save failed. There are non-zero chances that parse had exception and save was successful on
    // api so when user try saving same code we detect it's already in db. If we skip parsing, client
    // never gets parse status of new code. Thus we should always parse, it's pretty cheap and doesn't
    // matter much.
    // parse
    Optional<RunError> parseError = parseRun.parse(code);
    if (parseError.isPresent()) {
      VersionParseError versionParseError = new VersionParseError()
          .setVersionId(versionId).setError(parseError.get());
      return ResponseEntity.ok(versionParseError);
    }
    return ResponseEntity.ok().build();
  }
  
  @GetMapping("/versions/{versionIds}/parse")
  public ResponseEntity<List<VersionParseError>> parseVersions(
      @PathVariable @NotBlank String versionIds,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    List<VersionParseError> errors = new ArrayList<>();
    Map<Integer, String> vidToCode = testVersionProvider.getCodes(
        CommonUtil.commaDelToNumericList(versionIds),
        getUserId(userInfo));
    vidToCode.forEach((vId, code) -> {
      Optional<RunError> parseError = parseRun.parse(code);
      if (parseError.isPresent()) {
        VersionParseError versionParseError = new VersionParseError()
            .setVersionId(vId).setError(parseError.get());
        errors.add(versionParseError);
      }
    });
    return ResponseEntity.ok(errors);
  }
  
  // !! This should've been a GET but I found no good way to send an object via get, since a POST
  // can escape without having side effects, taking advantage of that spec and making it POST.
  @PostMapping("/projects/{projectId}/versions/{versionId}/dryRun")
  @SuppressWarnings("unused")
  public ResponseEntity<DryRunResult> dryRunVersion(
      @RequestBody @Validated DryRunConfig dryRunConfig,
      @PathVariable @Min(1) int projectId,
      @PathVariable @Min(1) int versionId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    // !! I've not verified browser and platform info from front end as it's a dry run, although
    // browser will be verified in zwl.
    ZwlDryRunProperties.Capabilities capabilities = new ZwlDryRunProperties.Capabilities() {
      @Nullable
      @Override
      public String getBrowserName() {
        return dryRunConfig.getBrowser().getName();
      }
  
      @Nullable
      @Override
      public String getBrowserVersion() {
        return dryRunConfig.getBrowser().getVersion();
      }
  
      @Nullable
      @Override
      public String getPlatformName() {
        return dryRunConfig.getPlatform();
      }
    };
    
    ZwlDryRunProperties.Variables variables = new ZwlDryRunProperties.Variables() {
      @Override
      public Map<String, String> getBuildVariables() {
        List<BuildVar> buildVars;
        if (dryRunConfig.getSelectedBuildVarIdPerKey().size() == 0) {
          buildVars = buildVarProvider.getBuildVars(projectId, userId, true);
        } else {
          buildVars = buildVarProvider.getPrimaryBuildVarsOverridingGiven(projectId, userId,
              dryRunConfig.getSelectedBuildVarIdPerKey());
        }
        return buildVars.stream().collect(Collectors.toMap(BuildVar::getKey, BuildVar::getValue));
      }
  
      @Nullable
      @Override
      public Map<String, String> getPreferences() {
        return null;
      }
  
      @Override
      public Map<String, String> getGlobal() {
        List<GlobalVar> globalVars = globalVarProvider.getGlobalVars(projectId, userId);
        return globalVars.stream().collect(Collectors.toMap(GlobalVar::getKey, GlobalVar::getValue));
      }
    };
    
    DryRunResult result = new DryRun(capabilities, variables)
        .run(testVersionProvider.getCode(versionId, userId));
    return ResponseEntity.ok(result);
  }
  
  @DeleteMapping("/versions/{versionId}")
  public ResponseEntity<Void> deleteFile(
      @PathVariable @Min(1) int versionId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    testVersionProvider.deleteVersion(versionId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @Validated
  private static class UpdateCodeRequest {
    
    @NotNull
    private String code;
  
    public String getCode() {
      return code;
    }
  
    public UpdateCodeRequest setCode(String code) {
      this.code = code;
      return this;
    }
  }
}
