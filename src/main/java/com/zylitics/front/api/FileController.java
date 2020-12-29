package com.zylitics.front.api;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.zylitics.front.model.File;
import com.zylitics.front.model.FileIdentifier;
import com.zylitics.front.provider.FileProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${app-short-version}/projects/{projectId}/files")
public class FileController extends AbstractController {
  
  private final FileProvider fileProvider;
  
  @Autowired
  public FileController(FileProvider fileProvider) {
    this.fileProvider = fileProvider;
  }
  
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<File> newFile(
      @Validated @RequestBody File file,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return ResponseEntity.ok(fileProvider.newFile(file, projectId, getUserId(userInfo)));
  }
  
  @GetMapping
  public ResponseEntity<List<FileIdentifier>> getFilesIdentifier(
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    return ResponseEntity.ok(fileProvider.getFilesIdentifier(projectId, getUserId(userInfo)));
  }
  
  @GetMapping("/getWithTests")
  public ResponseEntity<List<File>> getFilesWithTests(
      @RequestParam(required = false) String fileIdsFilter,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    List<Integer> fIds = new ArrayList<>();
    if (!Strings.isNullOrEmpty(fileIdsFilter)) {
      fIds = Splitter.on(",").omitEmptyStrings().trimResults()
          .splitToList(fileIdsFilter).stream().map(Integer::parseInt).collect(Collectors.toList());
    }
    return ResponseEntity.ok(fileProvider.getFilesWithTests(fIds, getUserId(userInfo)));
  }
  
  @SuppressWarnings("unused")
  @PatchMapping
  public ResponseEntity<Void> renameFile(
      @Validated @RequestBody File file,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    fileProvider.renameFile(file, projectId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/{fileId}")
  public ResponseEntity<Void> deleteFile(
      @PathVariable @Min(1) int fileId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo
  ) {
    fileProvider.deleteFile(fileId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
}
