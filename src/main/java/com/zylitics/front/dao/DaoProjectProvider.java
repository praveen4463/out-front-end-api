package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.Project;
import com.zylitics.front.model.User;
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
  
  private final Common common;
  
  @Autowired
  DaoProjectProvider(NamedParameterJdbcTemplate jdbc, Common common) {
    super(jdbc);
    this.common = common;
  }
  
  // Note: no sorting is done at server, client is responsible for sorting after receiving data. Client
  // should sort as per user's locale.
  @Override
  public List<Project> getProjects(int userId) {
    Preconditions.checkArgument(userId > 0, "userId is required");
    User user = common.getUserOwnProps(userId);
    
    String sql = "SELECT" +
        " bt_project_id" +
        ", name" +
        " FROM bt_project WHERE organization_id = :organization_id;";
    SqlParameterSource namedParams = new MapSqlParameterSource("organization_id",
        new SqlParameterValue(Types.INTEGER, user.getOrganizationId()));
    
    return jdbc.query(sql, namedParams, (rs, rowNum) ->
        new Project()
            .setId(rs.getInt("bt_project_id"))
            .setName(rs.getString("name")));
  }
  
  private void checkDupeProject(String name, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT count(*) FROM bt_project WHERE organization_id = :organization_id" +
        " AND name ILIKE lower(:name);";
  
    int matchingProjects = jdbc.query(sql,
        new SqlParamsBuilder()
            .withOrganization(user.getOrganizationId()).withVarchar("name", name).build(),
        CommonUtil.getSingleInt()).get(0);
    if (matchingProjects > 0) {
    
      throw new IllegalArgumentException("A project with the same name already exists");
    }
  }
  
  @Override
  public int saveNewProject(Project newProject, int userId) {
    Preconditions.checkNotNull(newProject, "newProject can't be null");
    User user = common.getUserOwnProps(userId);
    
    // first check whether there is a duplicate
    checkDupeProject(newProject.getName(), userId);
    
    String sql = "INSERT INTO bt_project (name, zluser_id, organization_id, create_date)" +
        " VALUES (:name, :zluser_id, :organization_id, :create_date) RETURNING bt_project_id;";
    
    return jdbc.query(sql, new SqlParamsBuilder(userId)
        .withOrganization(user.getOrganizationId())
        .withVarchar("name", newProject.getName())
        .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
  }
  
  @Override
  public Optional<Project> getProject(int projectId, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT name FROM bt_project WHERE bt_project_id = :bt_project_id\n" +
        "AND organization_id = :organization_id;";
    SqlParameterSource params = new SqlParamsBuilder()
        .withProject(projectId)
        .withOrganization(user.getOrganizationId())
        .build();
    List<Project> projects = jdbc.query(sql, params,
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
    User user = common.getUserOwnProps(userId);
    SqlParameterSource params = new SqlParamsBuilder()
        .withProject(projectId)
        .withOrganization(user.getOrganizationId())
        .withVarchar("name", name)
        .build();
    checkDupeProject(name, userId);
    
    String sql = "UPDATE bt_project SET name = :name WHERE bt_project_id = :bt_project_id\n" +
        "AND organization_id = :organization_id";
    CommonUtil.validateSingleRowDbCommit(jdbc.update(sql, params));
  }
  
  @Override
  public void deleteProject(int projectId, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "DELETE FROM bt_project WHERE bt_project_id = :bt_project_id\n" +
        "AND organization_id = :organization_id";
    CommonUtil.validateSingleRowDbCommit(
        jdbc.update(sql, new SqlParamsBuilder()
            .withProject(projectId)
            .withOrganization(user.getOrganizationId())
            .build()));
  }
}
