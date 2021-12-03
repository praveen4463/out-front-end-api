package com.zylitics.front.dao;

import com.zylitics.front.exception.UnauthorizedException;
import com.zylitics.front.model.User;
import com.zylitics.front.provider.UserProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
class Common extends AbstractDaoProvider {
  
  private final UserProvider userProvider;
  
  @Autowired
  Common(NamedParameterJdbcTemplate jdbc, UserProvider userProvider) {
    super(jdbc);
    this.userProvider = userProvider;
  }
  
  User getUserOwnProps(int userId) {
    return userProvider.getUser(userId, true)
        .orElseThrow(() -> new RuntimeException(userId + " not found."));
  }
  
  void verifyUsersProject(int projectId, User user) {
    String sql = "SELECT organization_id FROM bt_project WHERE bt_project_id = :bt_project_id;";
    List<Integer> projectOrgs = jdbc.query(sql,
        new SqlParamsBuilder().withProject(projectId).build(), CommonUtil.getSingleInt());
    if (projectOrgs.size() == 0) {
      throw new IllegalArgumentException("Project " + projectId + " wasn't found");
    }
    if (projectOrgs.get(0) != user.getOrganizationId()) {
      throw new UnauthorizedException(
          "User " + user.getId() + " doesn't have access on project " + projectId);
    }
  }
  
  void verifyUsersProject(int projectId, int userId) {
    User user = getUserOwnProps(userId);
    verifyUsersProject(projectId, user);
  }
  
  void verifyUsersBuild(int buildId, int userId) {
    User user = getUserOwnProps(userId);
    verifyUsersBuild(buildId, user);
  }
  
  void verifyUsersBuild(int buildId, User user) {
    String sql = "SELECT p.organization_id FROM bt_build AS b\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_build_id = :bt_build_id;";
    List<Integer> buildOrgs = jdbc.query(sql,
        new SqlParamsBuilder().withInteger("bt_build_id", buildId).build(),
        CommonUtil.getSingleInt());
    if (buildOrgs.size() == 0) {
      throw new IllegalArgumentException("Build " + buildId + " wasn't found");
    }
    if (buildOrgs.get(0) != user.getOrganizationId()) {
      throw new UnauthorizedException(
          "User " + user.getId() + " doesn't have access on build " + buildId);
    }
  }
}
