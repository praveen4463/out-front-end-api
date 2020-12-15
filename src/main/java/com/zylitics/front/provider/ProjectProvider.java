package com.zylitics.front.provider;

import com.zylitics.front.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectProvider {
  
  /**
   * Saves a new project and returns it's id.
   * @param newProject an instance of {@link NewProject}
   * @return An {@link Optional} with new project's id or empty {@link Optional}
   */
  Optional<Integer> saveNewProject(NewProject newProject);
  
  List<Project> getProjects(int userId);
}
