package com.zylitics.front.api;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.common.base.Preconditions;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.exception.UnauthorizedException;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.*;
import com.zylitics.front.services.RunnerService;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.front.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app-short-version}")
public class BuildController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(BuildController.class);
  
  private static final int COMPLETED_BUILD_SUMMARY_PAGE_SIZE = 20;
  
  private final APICoreProperties apiCoreProperties;
  
  private final Storage storage;
  
  private final BuildProvider buildProvider;
  
  private final UserProvider userProvider;
  
  private final BuildRequestProvider buildRequestProvider;
  
  private final TestVersionProvider testVersionProvider;
  
  private final BuildCapabilityProvider buildCapabilityProvider;
  
  private final BuildVMProvider buildVMProvider;
  
  private final BuildOutputProvider buildOutputProvider;
  
  private final BuildStatusProvider buildStatusProvider;
  
  private final ShotProvider shotProvider;
  
  private final BuildVarProvider buildVarProvider;
  
  private final GlobalVarProvider globalVarProvider;
  
  private final VMService vmService;
  
  private final RunnerService runnerService;
  
  public BuildController(BuildProvider buildProvider,
                         UserProvider userProvider,
                         BuildRequestProvider buildRequestProvider,
                         TestVersionProvider testVersionProvider,
                         VMService vmService,
                         RunnerService runnerService,
                         BuildCapabilityProvider buildCapabilityProvider,
                         BuildVMProvider buildVMProvider,
                         BuildOutputProvider buildOutputProvider,
                         BuildStatusProvider buildStatusProvider,
                         ShotProvider shotProvider,
                         BuildVarProvider buildVarProvider,
                         GlobalVarProvider globalVarProvider,
                         APICoreProperties apiCoreProperties,
                         Storage storage) {
    this.buildProvider = buildProvider;
    this.userProvider = userProvider;
    this.buildRequestProvider = buildRequestProvider;
    this.testVersionProvider = testVersionProvider;
    this.buildCapabilityProvider = buildCapabilityProvider;
    this.buildVMProvider = buildVMProvider;
    this.buildOutputProvider = buildOutputProvider;
    this.buildStatusProvider = buildStatusProvider;
    this.vmService = vmService;
    this.runnerService = runnerService;
    this.shotProvider = shotProvider;
    this.buildVarProvider = buildVarProvider;
    this.globalVarProvider = globalVarProvider;
    this.apiCoreProperties = apiCoreProperties;
    this.storage = storage;
  }
  
  /*
  When user issues new build request, we need to check whether they're within quota limits of
  parallel builds. The best way to do it without polluting build table with illegal entries is to
  create a separate table called build_request that will hold one entry for every request to new
  build. As a new request comes, put it into this table using build source and userId. Get user
  details before fetching all build request for user so that if multiple build request comes, they
  first put into this table, wait for sometime (fetching user details) and check all request for
  user to get records put by other concurrent request.
  If total records in build request are more than parallel limit, don't allow new build.
  If total IDE source records in build request are more than, don't allow new build.
  Perform other checks.
  If any of the above check fails and we don't create build for current request, delete it's record
  to free up current request occupation for a spot in build.
  When build is created, put buildRequestId in build table. Now when vm or new session are requested
  but fail, delete the record from here to free up current request's occupation.
  Once session is created, it is runner's responsibility to delete the record once build is completed.
  When source is IDE, runner deletes record once build is finished but before asset upload.
  When source is other than IDE, runner deletes record once everything is finished just before VM
  delete/update.
  This way, a build request record will denote a parallel quota occupation.
   */
  @PostMapping("/projects/{projectId}/builds")
  @SuppressWarnings("unused")
  public ResponseEntity<?> newBuild(
      @Validated @RequestBody BuildRunConfig buildRunConfig,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    int userId = getUserId(userInfo);
    BuildSourceType sourceType = buildRunConfig.getBuildSourceType();
    long buildRequestId = buildRequestProvider.newBuildRequest(new BuildRequest()
        .setBuildSourceType(sourceType).setUserId(userId));
    try {
      User user = userProvider.getUser(userId)
          .orElseThrow(() -> new UnauthorizedException("User not found"));
      UsersPlan usersPlan = user.getUsersPlan();
      Preconditions.checkArgument(usersPlan != null);
      List<BuildRequest> buildRequests = buildRequestProvider.getCurrentBuildRequests(userId);
      int totalBuildRequests = buildRequests.size();
  
      if (totalBuildRequests > usersPlan.getTotalParallel()) {
        markBuildRequestCompleted(buildRequestId);
        return sendError(HttpStatus.TOO_MANY_REQUESTS, "Total parallel builds limit reached." +
            " If you're starting several builds together, please slow down and wait for few" +
            " moments in between build runs");
      }
      if (sourceType == BuildSourceType.IDE) {
        if (buildRequests.stream()
            .filter(br -> br.getBuildSourceType() == BuildSourceType.IDE).count() > 1) {
          markBuildRequestCompleted(buildRequestId);
          return sendError(HttpStatus.TOO_MANY_REQUESTS, "You can start/run just one build from" +
              " IDE at a time. If a build is currently running, wait until all tests have" +
              " been processed");
        }
      }
      if (usersPlan.getPlanType() == PlanType.FREE &&
          usersPlan.getTotalMinutes() - usersPlan.getConsumedMinutes() < 1) {
        markBuildRequestCompleted(buildRequestId);
        return sendError(HttpStatus.FORBIDDEN, "You've exhausted plan's minutes quota" +
            ", please upgrade or contact us for additional testing minutes");
      }
      // parallel, minutes quota verified, let's verify code and jump on create build.
      if (testVersionProvider.anyVersionHasBlankCode(buildRunConfig.getVersionIds(), userId)) {
        markBuildRequestCompleted(buildRequestId);
        throw new IllegalArgumentException("All tests in a build must contain non empty code");
      }
      BuildIdentifier buildIdentifier =
          buildProvider.newBuild(buildRunConfig, buildRequestId, user, projectId);
      // send a request to create/find a VM
      BuildCapability buildCapability = buildCapabilityProvider
          .getBuildCapability(buildRunConfig.getBuildCapabilityId(), userId)
          .orElseThrow(RuntimeException::new);
      BuildVM buildVM;
      try {
        buildVM = vmService.newBuildVM(new NewBuildVM()
            .setDisplayResolution(buildRunConfig.getDisplayResolution())
            .setTimezone(buildRunConfig.getTimezone())
            .setBrowserName(buildCapability.getWdBrowserName())
            .setBrowserVersion(buildCapability.getWdBrowserVersion())
            .setOs(buildCapability.getServerOs()));
        // set deleteFromRunner
        buildVM.setDeleteFromRunner(sourceType != BuildSourceType.IDE);
      } catch (Throwable t) {
        LOG.error("Couldn't create buildVM", t);
        markBuildRequestCompleted(buildRequestId);
        return sendError(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't create a VM for this build." +
            " Please try in a few minutes or contact us if problem persists");
      }
      // save buildVM details to vm table
      buildProvider.createAndUpdateVM(buildVM, buildIdentifier.getBuildId());
      // start new session as we've a VM
      String sessionId;
      try {
        sessionId =
            runnerService.newSession(buildVM.getInternalIp(), buildIdentifier.getBuildId());
      } catch (Throwable t) {
        LOG.error("Couldn't start a new session", t);
        markBuildRequestCompleted(buildRequestId);
        return sendError(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred" +
            " while creating new session. We've been notified and this should be fixed very soon");
      }
      buildProvider.updateSession(sessionId, buildIdentifier.getBuildId());
      return ResponseEntity.ok(new NewBuildResponse().setBuildIdentifier(buildIdentifier)
          .setSessionId(sessionId));
    } catch (Throwable t) {
      buildRequestProvider.markBuildRequestCompleted(buildRequestId);
      throw t;
    }
  }
  
  @PostMapping("/builds/{buildId}/reRun")
  @SuppressWarnings("unused")
  public ResponseEntity<?> reRun(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    // rerun this build.
    return null;
  }
  
  // start and end dates must be in UTC and ISO_DATE_TIME format
  @GetMapping("/projects/{projectId}/builds/getCompletedBuildSummary")
  public ResponseEntity<?> getCompletedBuildSummary(
      @PathVariable @Min(1) int projectId,
      @RequestParam String start,
      @RequestParam String end,
      @RequestParam(required = false) TestStatus status,
      @RequestParam(required = false) String brw,
      @RequestParam(required = false) String brwV,
      @RequestParam(required = false) String os,
      @RequestParam(required = false) Integer after,
      @RequestParam(required = false) Integer before,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    CompletedBuildSummaryFilters filters;
    try {
      filters = new CompletedBuildSummaryFilters()
          .setStartDateUTC(DateTimeUtil.fromUTCISODateTimeString(start))
          .setEndDateUTC(DateTimeUtil.fromUTCISODateTimeString(end))
          .setFinalStatus(status)
          .setBrowserName(brw)
          .setBrowserVersion(brwV)
          .setOs(os)
          .setAfterBuildId(after)
          .setBeforeBuildId(before);
    } catch (DateTimeParseException dateTimeParseException) {
      return sendError(HttpStatus.UNPROCESSABLE_ENTITY, "Supplied dates are in unsupported format");
    }
    // although builds are immutable, there could be new ones and also user may delete some thus
    // since the same query may return different results, we can't cache results.
    return ResponseEntity.ok(buildProvider.getCompletedBuildsSummaryWithPaging(filters,
        COMPLETED_BUILD_SUMMARY_PAGE_SIZE,
        projectId,
        getUserId(userInfo)));
  }
  
  @SuppressWarnings("unused")
  @PatchMapping("/builds/{buildId}/stopBuild")
  public ResponseEntity<?> stopBuild(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    int userId = getUserId(userInfo);
    Optional<BuildVM> buildVM = buildVMProvider.getBuildVMByBuild(buildId, userId);
    if (!buildVM.isPresent()) {
      throw new RuntimeException("Couldn't find a VM record for build " + buildId);
    }
    boolean result = runnerService.stopBuild(buildVM.get().getInternalIp(), buildId);
    if (!result) {
      return sendError(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't stop this build, an internal" +
          " server error occurred. We've been notified and this should be fixed very soon");
    }
    return ResponseEntity.ok().build();
  }
  
  @GetMapping("/builds/{buildId}/versions/{versionId}/getBuildStatusOutput")
  public ResponseEntity<BuildStatusOutput> getBuildStatusOutput(
      @PathVariable @Min(1) int buildId,
      @PathVariable @Min(1) int versionId,
      @RequestParam(required = false) String nextOutputToken,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    Optional<BuildStatus> buildStatusOptional = buildStatusProvider.getBuildStatus(buildId,
        versionId, getUserId(userInfo));
    if (!buildStatusOptional.isPresent()) {
      return ResponseEntity.ok().build(); // return empty when no build status is found.
      // Note that if user doesn't have access to the asked build status, an empty status is returned
      // too. So unauthorized accessed will never read statuses.
      // When no status, we don't fetch output.
      // When a valid client is making statusOutput calls, an empty response may be returned if
      // client calls this endpoint too quickly after knowing a session is created as runner may be
      // busy in doing some tasks before initiating first version. Thus clients should expect
      // empty responses in the beginning until they see some status. There may be an empty response
      // in between version changes too as runner has to pick next version, do some tasks and insert
      // new version.
    }
    // When build status available, fetch output
    Optional<BuildOutput> buildOutputOptional =
        buildOutputProvider.getOutput(buildId, versionId, nextOutputToken);
    // transform buildStatus into BuildStatusOutput
    BuildStatus buildStatus = buildStatusOptional.get();
    long timeTaken = 0;
    RunError error = null;
    if (buildStatus.getStartDate() != null && buildStatus.getEndDate() != null) {
      timeTaken = ChronoUnit.MILLIS.between(buildStatus.getStartDate(), buildStatus.getEndDate());
    }
    if (buildStatus.getError() != null) {
      // when error is not null, positions must be available, don't check that, I want exception
      // thrown if that's not true.
      error = new RunError()
          .setMsg(buildStatus.getError())
          .setFrom(CommonUtil.getLineInfo(buildStatus.getErrorFromPos()))
          .setTo(CommonUtil.getLineInfo(buildStatus.getErrorToPos()));
    }
    BuildStatusOutput buildStatusOutput = new BuildStatusOutput()
        .setStatus(buildStatus.getStatus())
        .setCurrentLine(buildStatus.getZwlExecutingLine())
        .setError(error)
        .setTimeTaken(timeTaken);
    if (buildOutputOptional.isPresent()) {
      BuildOutput buildOutput = buildOutputOptional.get();
      buildStatusOutput
          .setOutput(buildOutput.getOutputsWithLineBreak())
          .setNextOutputToken(buildOutput.getNextOutputToken());
    }
    return ResponseEntity.ok(buildStatusOutput);
  }
  
  @GetMapping("/builds/{buildId}/getLatestShot")
  public ResponseEntity<String> getLatestShot(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    buildProvider.verifyUsersBuild(buildId, getUserId(userInfo));
    Optional<String> latestShot = shotProvider.getLatestShot(buildId);
    return latestShot.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok().build());
  }
  
  @GetMapping("/builds/{buildId}/getShotBasicDetails")
  public ResponseEntity<ShotBasicDetails> getShotBasicDetails(
      @PathVariable @Min(1) int buildId,
      @RequestParam(required = false) Integer versionId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    buildProvider.verifyUsersBuild(buildId, getUserId(userInfo));
    Optional<ShotBasicDetails> shotBasicDetails = shotProvider
        .getShotBasicDetails(buildId, versionId);
    return shotBasicDetails.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok().build());
  }
  
  @GetMapping("/builds/{buildId}/getCapturedBuildCapability")
  public ResponseEntity<BuildCapability> getCapturedBuildCapability(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    int userId = getUserId(userInfo);
    Optional<BuildCapability> buildCapabilityOptional =
        buildCapabilityProvider.getCapturedCapability(buildId, userId);
    if (!buildCapabilityOptional.isPresent()) {
      throw new UnauthorizedException(
          "User " + userId + " doesn't have access on build " + buildId);
    }
    return Common.addShortTermCacheControl(ResponseEntity.ok()).body(buildCapabilityOptional.get());
  }
  
  // send back a list of variables rather than Map so that it's easier on client to sort the array
  @GetMapping("/builds/{buildId}/getCapturedBuildVars")
  public ResponseEntity<List<CapturedVariable>> getCapturedBuildVars(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return Common.addShortTermCacheControl(ResponseEntity.ok())
        .body(buildVarProvider.getCapturedBuildVars(buildId, getUserId(userInfo)));
  }
  
  // send back a list of variables rather than Map so that it's easier on client to sort the array
  @GetMapping("/builds/{buildId}/getCapturedGlobalVars")
  public ResponseEntity<List<CapturedVariable>> getCapturedGlobalVars(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return Common.addShortTermCacheControl(ResponseEntity.ok())
        .body(globalVarProvider.getCapturedGlobalVars(buildId, getUserId(userInfo)));
  }
  
  @GetMapping("/builds/{buildId}/getRunnerPreferences")
  public ResponseEntity<RunnerPreferences> getRunnerPreferences(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    int userId = getUserId(userInfo);
    Optional<RunnerPreferences> runnerPreferencesOptional =
        buildProvider.getRunnerPrefs(buildId, getUserId(userInfo));
    if (!runnerPreferencesOptional.isPresent()) {
      throw new UnauthorizedException(
          "User " + userId + " doesn't have access on build " + buildId);
    }
    return Common.addShortTermCacheControl(ResponseEntity.ok()).body(runnerPreferencesOptional.get());
  }
  
  @GetMapping("/builds/{buildId}/getCompletedBuildDetails")
  public ResponseEntity<CompletedBuildDetails> getCompletedBuildDetails(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    int userId = getUserId(userInfo);
    Optional<CompletedBuildDetails> completedBuildDetailsOptional =
        buildProvider.getCompletedBuildDetails(buildId, userId);
    if (!completedBuildDetailsOptional.isPresent()) {
      return ResponseEntity.ok().build(); // let caller handle this case
    }
    CompletedBuildDetails completedBuildDetails = completedBuildDetailsOptional.get();
    // if runner has not completed processing the build, don't check other things that may be in
    // progress.
    if (completedBuildDetails.getAllDoneDate() == null) {
      return ResponseEntity.ok(completedBuildDetails);
    }
    // add remaining things into details from cloud
    String buildDir = Common.getBuildDirName(buildId);
    APICoreProperties.Storage storageProps = apiCoreProperties.getStorage();
    Blob driverLogsBlob = getDriverLogsBlob(storageProps, buildDir);
    Blob perfLogsBlob = getPerfLogsBlob(storageProps, buildDir);
    Iterator<Blob> elementShotIterator = getElementShotIterator(storageProps, buildDir);
    if (elementShotIterator.hasNext()) {
      Blob blob = elementShotIterator.next();
      if (blob.getName().endsWith(".png")) {
        completedBuildDetails.setElemShotsAvailable(true);
      }
    }
    completedBuildDetails.setDriverLogsAvailable(driverLogsBlob != null);
    completedBuildDetails.setPerfLogsAvailable(perfLogsBlob != null);
    // since a build is immutable and it's all fully done, we want browser to cache the response.
    return Common.addShortTermCacheControl(ResponseEntity.ok()).body(completedBuildDetails);
  }
  
  @GetMapping("/builds/{buildId}/getDriverLogs")
  public ResponseEntity<String> getDriverLogs(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    String buildDir = Common.getBuildDirName(buildId);
    APICoreProperties.Storage storageProps = apiCoreProperties.getStorage();
    return sendLogsResponse(buildId, getUserId(userInfo),
        getDriverLogsBlob(storageProps, buildDir));
  }
  
  @GetMapping("/builds/{buildId}/getPerformanceLogs")
  public ResponseEntity<String> getPerformanceLogs(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    String buildDir = Common.getBuildDirName(buildId);
    APICoreProperties.Storage storageProps = apiCoreProperties.getStorage();
    return sendLogsResponse(buildId, getUserId(userInfo),
        getPerfLogsBlob(storageProps, buildDir));
  }
  
  // TODO: I am sending log file contents as String, being log files can be large, there may be
  //  issues both at api and client. Keep a watch and may be later we will have to lazy load the
  //  file or give just option to download.
  private ResponseEntity<String> sendLogsResponse(int buildId, int userId, Blob blob) {
    buildProvider.verifyUsersBuild(buildId, userId);
    if (blob == null) {
      return ResponseEntity.ok().build();
    }
    byte[] file = new FileDownload().download(blob);
    return Common.addShortTermCacheControl(ResponseEntity.ok())
        .body(new String(file, StandardCharsets.UTF_8));
  }
  
  @GetMapping("/builds/{buildId}/getElementShotNames")
  public ResponseEntity<List<String>> getElementShotNames(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    buildProvider.verifyUsersBuild(buildId, getUserId(userInfo));
    String buildDir = Common.getBuildDirName(buildId);
    APICoreProperties.Storage storageProps = apiCoreProperties.getStorage();
    Iterator<Blob> elementShotIterator = getElementShotIterator(storageProps, buildDir);
    List<String> shots = new ArrayList<>();
    while (elementShotIterator.hasNext()) {
      Blob blob = elementShotIterator.next();
      if (blob.getName().endsWith(".png")) {
        shots.add(CommonUtil.getStorageFileNameWithoutPrefix(blob.getName()));
      }
    }
    if (shots.size() == 0) {
      return ResponseEntity.ok().build();
    }
    // send a cached response as shot list is immutable
    return Common.addShortTermCacheControl(ResponseEntity.ok()).body(shots);
  }
  
  // for now don't cache any result as this endpoint could be used from places that might be polling
  // for output such as 'running builds' and try to cache at client per needs.
  @GetMapping("/builds/{buildId}/versions/{versionId}/getVersionOutputDetails")
  public ResponseEntity<BuildOutputDetailsByVersion> getVersionOutputDetails(
      @PathVariable @Min(1) int buildId,
      @PathVariable @Min(1) int versionId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    Optional<BuildOutputDetailsByVersion> output =
        buildOutputProvider.getVersionOutputDetails(buildId, versionId, getUserId(userInfo));
    return output.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok().build());
  }
  
  // for now don't cache any result as this endpoint could be used from places that might be polling
  // for output such as 'running builds' and try to cache at client per needs.
  @GetMapping("/builds/{buildId}/getBuildOutputDetails")
  public ResponseEntity<List<BuildOutputDetailsByVersion>> getBuildOutputDetails(
      @PathVariable @Min(1) int buildId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    List<BuildOutputDetailsByVersion> outputs =
        buildOutputProvider.getBuildOutputDetails(buildId, getUserId(userInfo));
    return ResponseEntity.ok(outputs);
  }
  
  @GetMapping("/builds/{buildId}/versions/{versionId}/getCapturedCode")
  public ResponseEntity<String> getCapturedCode(
      @PathVariable @Min(1) int buildId,
      @PathVariable @Min(1) int versionId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return Common.addShortTermCacheControl(ResponseEntity.ok())
        .body(buildProvider.getCapturedCode(buildId, versionId, getUserId(userInfo)));
  }
  
  private Blob getDriverLogsBlob(APICoreProperties.Storage storageProps, String buildDir) {
    return storage.get(BlobId.of(storageProps.getServerLogsBucket(),
        Common.getBlobName(buildDir,
            storageProps.getDriverLogsDir(),
            storageProps.getDriverLogsFile())));
  }
  
  private Blob getPerfLogsBlob(APICoreProperties.Storage storageProps, String buildDir) {
    return storage.get(BlobId.of(storageProps.getServerLogsBucket(),
        Common.getBlobName(buildDir,
            storageProps.getBrowserPerfLogsDir(),
            storageProps.getBrowserPerfLogsFile())));
  }
  
  private Iterator<Blob> getElementShotIterator(APICoreProperties.Storage storageProps,
                                                String buildDir) {
    Page<Blob> elementShotsBlobs = storage.list(storageProps.getElemShotsBucket(),
        Storage.BlobListOption.prefix(buildDir));
    return elementShotsBlobs.iterateAll().iterator();
  }
  
  private void markBuildRequestCompleted(long buildRequestId) {
    buildRequestProvider.markBuildRequestCompleted(buildRequestId);
  }
}
