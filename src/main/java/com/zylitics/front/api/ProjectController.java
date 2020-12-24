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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app-short-version}/projects")
public class ProjectController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
  
  private static final int MAX_PROJECT_NAME_LENGTH = 50;
  
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
  
    Preconditions.checkArgument(project.getName().length() <= MAX_PROJECT_NAME_LENGTH,
        " Project name can't contain more than " + MAX_PROJECT_NAME_LENGTH + " characters");
    
    Optional<Integer> projectId = projectProvider.saveNewProject(project, userId);
    
    if (!projectId.isPresent()) {
      throw new RuntimeException("Couldn't create project " + project.getName());
    }
    
    return ResponseEntity.ok(new Project().setId(projectId.get()).setName(project.getName()));
  }
  
  @SuppressWarnings("unused")
  @GetMapping
  public ResponseEntity<List<Project>> getProjects(
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
  
    List<Project> projects = projectProvider.getProjects(userId);
    
    return ResponseEntity.ok(projects);
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
}
