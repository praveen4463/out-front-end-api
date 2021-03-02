package com.zylitics.front.provider;

import com.zylitics.front.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectProvider {
  
  /**
   * Saves a new project and returns it's id.
   * @param project an instance of {@link Project}
   * @return new project's id
   */
  int saveNewProject(Project project, int userId);
  
  List<Project> getProjects(int userId);
  
  Optional<Project> getProject(int projectId, int userId);
  
  void renameProject(String name, int projectId, int userId);
  
  void deleteProject(int projectId, int userId);
}
