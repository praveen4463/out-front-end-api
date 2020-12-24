package com.zylitics.front.provider;

import com.zylitics.front.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectProvider {
  
  /**
   * Saves a new project and returns it's id.
   * @param project an instance of {@link Project}
   * @return An {@link Optional} with new project's id or empty {@link Optional}
   */
  Optional<Integer> saveNewProject(Project project, int userId);
  
  List<Project> getProjects(int userId);
}
