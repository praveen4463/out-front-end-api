package com.zylitics.front.provider;

import com.zylitics.front.model.Project;

import java.util.List;

public interface ProjectProvider {
  
  /**
   * Saves a new project and returns it's id.
   * @param project an instance of {@link Project}
   * @return new project's id
   */
  int saveNewProject(Project project, int userId);
  
  List<Project> getProjects(int userId);
}
