package com.zylitics.front.dao;

import com.zylitics.front.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class Common extends AbstractDaoProvider {
  
  @Autowired
  Common(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  void verifyUsersProject(int projectId, int userId) {
    String sql = "SELECT zluser_id FROM bt_project WHERE bt_project_id = :bt_project_id;";
    Integer projectUserId = jdbc.queryForObject(sql,
        new SqlParamsBuilder().withProject(projectId).build(), Integer.class);
    if (projectUserId == null || projectUserId != userId) {
      throw new UnauthorizedException(
          "User " + userId +" doesn't have access on project " + projectId);
    }
  }
}
