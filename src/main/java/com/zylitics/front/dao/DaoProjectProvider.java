package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.Project;
import com.zylitics.front.provider.ProjectProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.*;

@Repository
class DaoProjectProvider extends AbstractDaoProvider implements ProjectProvider {
  
  @Autowired
  DaoProjectProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  // Note: no sorting is done at server, client is responsible for sorting after receiving data. Client
  // should sort as per user's locale.
  @Override
  public List<Project> getProjects(int userId) {
    Preconditions.checkArgument(userId > 0, "userId is required");
    
    String sql = "SELECT" +
        " bt_project_id" +
        ", name" +
        " FROM bt_project WHERE zluser_id = :zluser_id;";
    SqlParameterSource namedParams = new MapSqlParameterSource("zluser_id",
        new SqlParameterValue(Types.INTEGER, userId));
    
    return jdbc.query(sql, namedParams, (rs, rowNum) ->
        new Project()
            .setId(rs.getInt("bt_project_id"))
            .setName(rs.getString("name")));
  }
  
  private void checkDupeProject(String name, int userId) {
    String sql = "SELECT count(*) FROM bt_project WHERE zluser_id = :zluser_id" +
        " AND name ILIKE lower(:name);";
  
    int matchingProjects = jdbc.query(sql,
        new SqlParamsBuilder(userId).withVarchar("name", name).build(),
        CommonUtil.getSingleInt()).get(0);
    if (matchingProjects > 0) {
      throw new IllegalArgumentException("A project with the same name already exists");
    }
  }
  
  @Override
  public int saveNewProject(Project newProject, int userId) {
    Preconditions.checkNotNull(newProject, "newProject can't be null");
    
    // first check whether there is a duplicate
    checkDupeProject(newProject.getName(), userId);
    
    String sql = "INSERT INTO bt_project (name, zluser_id, create_date)" +
        " VALUES (:name, :zluser_id, :create_date) RETURNING bt_project_id;";
    
    return jdbc.query(sql, new SqlParamsBuilder(userId)
        .withVarchar("name", newProject.getName())
        .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
  }
  
  @Override
  public Optional<Project> getProject(int projectId, int userId) {
    String sql = "SELECT name FROM bt_project WHERE bt_project_id = :bt_project_id\n" +
        "AND zluser_id = :zluser_id";
    List<Project> projects = jdbc.query(sql, new SqlParamsBuilder(projectId, userId).build(),
        (rs, rowNum) -> new Project()
            .setId(projectId)
            .setName(rs.getString("name")));
    if (projects.size() > 0) {
      return Optional.of(projects.get(0));
    }
    return Optional.empty();
  }
  
  @Override
  public void renameProject(String name, int projectId, int userId) {
    checkDupeProject(name, userId);
    
    String sql = "UPDATE bt_project SET name = :name WHERE bt_project_id = :bt_project_id\n" +
        "AND zluser_id = :zluser_id";
    CommonUtil.validateSingleRowDbCommit(jdbc.update(sql,
        new SqlParamsBuilder(projectId, userId).withVarchar("name", name).build()));
  }
  
  @Override
  public void deleteProject(int projectId, int userId) {
    String sql = "DELETE FROM bt_project WHERE bt_project_id = :bt_project_id\n" +
        "AND zluser_id = :zluser_id";
    CommonUtil.validateSingleRowDbCommit(
        jdbc.update(sql, new SqlParamsBuilder(projectId, userId).build()));
  }
}
