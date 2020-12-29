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
  
  @Override
  public int saveNewProject(Project newProject, int userId) {
    Preconditions.checkNotNull(newProject, "newProject can't be null");
    
    // first check whether there is a duplicate
    String sql = "SELECT count(*) FROM bt_project WHERE zluser_id = :zluser_id" +
        " AND name ILIKE lower(:name);";
  
    int matchingProjects = jdbc.query(sql,
        new SqlParamsBuilder(userId).withVarchar("name", newProject.getName()).build(),
        CommonUtil.getSingleInt()).get(0);
    if (matchingProjects > 0) {
      throw new IllegalArgumentException("A project with the same name already exists");
    }
    
    sql = "INSERT INTO bt_project (name, zluser_id, create_date)" +
        " VALUES (:name, :zluser_id, :create_date) RETURNING bt_project_id;";
    
    return jdbc.query(sql, new SqlParamsBuilder(userId)
        .withVarchar("name", newProject.getName())
        .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
  }
}
