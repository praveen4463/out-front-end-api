package com.zylitics.front.api;

import com.zylitics.front.model.TestVersion;
import com.zylitics.front.provider.TestVersionProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;

@RestController
@RequestMapping("${app-short-version}/versions")
public class TestVersionController extends AbstractController {
  
  private final TestVersionProvider testVersionProvider;
  
  public TestVersionController(TestVersionProvider testVersionProvider) {
    this.testVersionProvider = testVersionProvider;
  }
  
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<TestVersion> newVersion(
      @Validated @RequestBody TestVersion testVersion,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return ResponseEntity.ok(testVersionProvider.newVersion(testVersion, getUserId(userInfo)));
  }
  
  @SuppressWarnings("unused")
  @PatchMapping
  public ResponseEntity<Void> renameVersion(
      @Validated @RequestBody TestVersion testVersion,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    testVersionProvider.renameVersion(testVersion, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/{versionId}")
  public ResponseEntity<Void> deleteFile(
      @PathVariable @Min(1) int versionId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    testVersionProvider.deleteVersion(versionId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
}
