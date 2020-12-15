package com.zylitics.front.api;

import com.zylitics.front.SecretsManager;
import com.zylitics.front.http.NewProjectRequest;
import com.zylitics.front.http.NewProjectResponse;
import com.zylitics.front.model.Project;
import com.zylitics.front.provider.NewProject;
import com.zylitics.front.provider.ProjectProvider;
import com.zylitics.front.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${app-short-version}/projects")
public class ProjectController extends AbstractController {
  
  private static final Logger LOG = LoggerFactory.getLogger(ProjectController.class);
  
  private final ProjectProvider projectProvider;
  
  @Autowired
  public ProjectController(ProjectProvider projectProvider, SecretsManager secretsManager) {
    this(projectProvider, secretsManager, Clock.systemUTC());
  }
  
  public ProjectController(ProjectProvider projectProvider, SecretsManager secretsManager,
                           Clock clock) {
    super(secretsManager, clock);
    this.projectProvider = projectProvider;
  }
  
  @SuppressWarnings("unused")
  @PostMapping
  public ResponseEntity<NewProjectResponse> newProject(
      @Validated @RequestBody NewProjectRequest newProjectRequest
      , @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    LOG.info("received request to run: {}", newProjectRequest.toString());
    
    int userId = getUserId(userInfo);
    
    String projectName = newProjectRequest.getName();
    
    Optional<Integer> projectId = projectProvider.saveNewProject(
        new NewProject(projectName, userId, DateTimeUtil.getCurrent(clock)));
    
    if (!projectId.isPresent()) {
      throw new RuntimeException("Couldn't create project " + projectName);
    }
    
    return ResponseEntity.ok(new NewProjectResponse().setId(projectId.get()).setName(projectName));
  }
  
  @SuppressWarnings("unused")
  @GetMapping
  public ResponseEntity<List<Project>> getProjects(
      @RequestHeader(USER_INFO_REQ_HEADER) String userInfo) {
    int userId = getUserId(userInfo);
  
    List<Project> projects = projectProvider.getProjects(userId);
    
    return ResponseEntity.ok(projects);
  }
}
