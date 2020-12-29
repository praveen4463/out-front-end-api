package com.zylitics.front.api;

import com.zylitics.front.model.Test;
import com.zylitics.front.provider.TestProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;

@RestController
@RequestMapping("${app-short-version}/tests")
public class TestController extends AbstractController {
  
  private final TestProvider testProvider;
  
  public TestController(TestProvider testProvider) {
    this.testProvider = testProvider;
  }
  
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<Test> newTest(
      @Validated @RequestBody Test test,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return ResponseEntity.ok(testProvider.newTest(test, getUserId(userInfo)));
  }
  
  @SuppressWarnings("unused")
  @PatchMapping
  public ResponseEntity<Void> renameTest(
      @Validated @RequestBody Test test,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    testProvider.renameTest(test, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/{testId}")
  public ResponseEntity<Void> deleteFile(
      @PathVariable @Min(1) int testId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    testProvider.deleteTest(testId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
}
