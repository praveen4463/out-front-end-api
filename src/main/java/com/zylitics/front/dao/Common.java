package com.zylitics.front.dao;

import com.zylitics.front.exception.UnauthorizedException;
import com.zylitics.front.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class Common extends AbstractDaoProvider {
  
  @Autowired
  Common(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  void verifyUsersProject(int projectId, int userId) {
    String sql = "SELECT zluser_id FROM bt_project WHERE bt_project_id = :bt_project_id;";
    List<Integer> projectUserIds = jdbc.query(sql,
        new SqlParamsBuilder().withProject(projectId).build(), CommonUtil.getSingleInt());
    if (projectUserIds.size() == 0) {
      throw new IllegalArgumentException("Project " + projectId + " wasn't found");
    }
    if (projectUserIds.get(0) != userId) {
      throw new UnauthorizedException(
          "User " + userId + " doesn't have access on project " + projectId);
    }
  }
  
  void verifyUsersBuild(int buildId, int userId) {
    String sql = "SELECT p.zluser_id FROM bt_build AS b\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_build_id = :bt_build_id;";
    List<Integer> buildUserIds = jdbc.query(sql,
        new SqlParamsBuilder().withInteger("bt_build_id", buildId).build(),
        CommonUtil.getSingleInt());
    if (buildUserIds.size() == 0) {
      throw new IllegalArgumentException("Build " + buildId + " wasn't found");
    }
    if (buildUserIds.get(0) != userId) {
      throw new UnauthorizedException(
          "User " + userId + " doesn't have access on build " + buildId);
    }
  }
}
