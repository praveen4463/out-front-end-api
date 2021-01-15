package com.zylitics.front.api;

import com.zylitics.front.exception.UnauthorizedException;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.*;
import com.zylitics.front.services.RunnerService;
import com.zylitics.front.util.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app-short-version}")
public class BuildController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(BuildController.class);
  
  private final BuildProvider buildProvider;
  
  private final UserProvider userProvider;
  
  private final BuildRequestProvider buildRequestProvider;
  
  private final TestVersionProvider testVersionProvider;
  
  private final BuildCapabilityProvider buildCapabilityProvider;
  
  private final BuildVMProvider buildVMProvider;
  
  private final ZwlProgramOutputProvider zwlProgramOutputProvider;
  
  private final BuildStatusProvider buildStatusProvider;
  
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
                         ZwlProgramOutputProvider zwlProgramOutputProvider,
                         BuildStatusProvider buildStatusProvider) {
    this.buildProvider = buildProvider;
    this.userProvider = userProvider;
    this.buildRequestProvider = buildRequestProvider;
    this.testVersionProvider = testVersionProvider;
    this.buildCapabilityProvider = buildCapabilityProvider;
    this.buildVMProvider = buildVMProvider;
    this.zwlProgramOutputProvider = zwlProgramOutputProvider;
    this.buildStatusProvider = buildStatusProvider;
    this.vmService = vmService;
    this.runnerService = runnerService;
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
      List<BuildRequest> buildRequests = buildRequestProvider.getCurrentBuildRequests(userId);
      int totalBuildRequests = buildRequests.size();
  
      if (totalBuildRequests > user.getTotalParallel()) {
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
      if (user.getPlanType() == PlanType.FREE &&
          user.getTotalMinutes() - user.getConsumedMinutes() < 1) {
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
  
  @DeleteMapping("/builds/{buildId}")
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
    Optional<ZwlProgramOutput> zwlProgramOutputOptional =
        zwlProgramOutputProvider.getOutput(buildId, versionId, nextOutputToken);
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
    if (zwlProgramOutputOptional.isPresent()) {
      ZwlProgramOutput zwlProgramOutput = zwlProgramOutputOptional.get();
      String output = null;
      if (zwlProgramOutput.getOutputs() != null) {
        output = String.join("\n", zwlProgramOutput.getOutputs());
      }
      buildStatusOutput
          .setOutput(output)
          .setNextOutputToken(zwlProgramOutput.getNextOutputToken());
    }
    return ResponseEntity.ok(buildStatusOutput);
  }
  
  private void markBuildRequestCompleted(long buildRequestId) {
    buildRequestProvider.markBuildRequestCompleted(buildRequestId);
  }
}
