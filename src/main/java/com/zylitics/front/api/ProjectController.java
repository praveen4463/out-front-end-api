package com.zylitics.front.api;

import com.google.common.base.Preconditions;
import com.zylitics.front.SecretsManager;
import com.zylitics.front.model.Project;
import com.zylitics.front.provider.ProjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app-short-version}/projects")
public class ProjectController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
  
  private final ProjectProvider projectProvider;
  
  // require secretsManager only in this controller for closing it as onContextRefreshedEvent can't
  // be run in Launcher
  private final SecretsManager secretsManager;
  
  @Autowired
  public ProjectController(ProjectProvider projectProvider, SecretsManager secretsManager) {
    this.projectProvider = projectProvider;
    this.secretsManager = secretsManager;
  }
  
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<Project> newProject(
      @Validated @RequestBody Project project,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    
    int projectId = projectProvider.saveNewProject(project, userId);
    
    return ResponseEntity.ok(new Project().setId(projectId).setName(project.getName()));
  }
  
  @SuppressWarnings("unused")
  @GetMapping
  public ResponseEntity<List<Project>> getProjects(
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
  
    List<Project> projects = projectProvider.getProjects(userId);
    
    return ResponseEntity.ok(projects);
  }
  
  @GetMapping("/{projectId}")
  public ResponseEntity<Project> getProject(
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
    Optional<Project> projectOptional = projectProvider.getProject(projectId, userId);
    Preconditions.checkArgument(projectOptional.isPresent(), "Invalid projectId " + projectId);
    return ResponseEntity.ok(projectOptional.get());
  }
  
  @SuppressWarnings("unused")
  @PatchMapping("/{projectId}/renameProject")
  public ResponseEntity<Void> renameProject(
      @RequestBody @Validated RenameProjectRequest renameProjectRequest,
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    projectProvider.renameProject(renameProjectRequest.getName(), projectId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @DeleteMapping("/{projectId}")
  public ResponseEntity<Void> deleteProject(
      @PathVariable @Min(1) int projectId,
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    projectProvider.deleteProject(projectId, getUserId(userInfo));
    return ResponseEntity.ok().build();
  }
  
  @EventListener(ContextRefreshedEvent.class)
  void onContextRefreshedEvent() throws IOException {
    LOG.debug("ContextRefreshEvent was triggered");
    
    // Close SecretsManager once all beans that required it are loaded.
    // A new manager is created when needed.
    if (secretsManager != null) {
      LOG.debug("secretsManager will now close");
      secretsManager.close();
    }
  }
  
  @Validated
  private static class RenameProjectRequest {
    
    @NotBlank
    private String name;
  
    public String getName() {
      return name;
    }
  
    public RenameProjectRequest setName(String name) {
      this.name = name;
      return this;
    }
  }
}
