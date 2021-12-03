package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.BuildVar;
import com.zylitics.front.model.CapturedVariable;
import com.zylitics.front.model.User;
import com.zylitics.front.provider.BuildVarProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.JDBCType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Repository
public class DaoBuildVarProvider extends AbstractDaoProvider implements BuildVarProvider {
  
  private final Common common;
  
  private final TransactionTemplate transactionTemplate;
  
  DaoBuildVarProvider(NamedParameterJdbcTemplate jdbc, Common common,
                      TransactionTemplate transactionTemplate) {
    super(jdbc);
    this.common = common;
    this.transactionTemplate = transactionTemplate;
  }
  
  @Override
  public int newBuildVar(BuildVar buildVar, int projectId, int userId) {
    Preconditions.checkNotNull(buildVar, "buildVar is required");
    
    common.verifyUsersProject(projectId, userId); // check access initially as there are several
    // queries here and I don't want a join on all
    
    // check dupe build var
    String checkExistenceSql = "SELECT count(*) FROM zwl_build_variables\n" +
        "WHERE key ILIKE lower(:key) AND value = :value AND bt_project_id = :bt_project_id";
    int matchingVar = jdbc.query(checkExistenceSql,
        new SqlParamsBuilder()
            .withProject(projectId)
            .withVarchar("key", buildVar.getKey())
            .withOther("value", buildVar.getValue()).build(),
        CommonUtil.getSingleInt()).get(0);
    if (matchingVar > 0) {
      throw new IllegalArgumentException("Can't add build variable, given key and value already" +
          " exists");
    }
  
    // check whether given build var's key already exists
    String checkKeySql = "SELECT key FROM zwl_build_variables\n" +
        "WHERE bt_project_id = :bt_project_id AND key ILIKE lower(:key) group by key";
    List<String> matchingKeys = jdbc.query(checkKeySql,
        new SqlParamsBuilder()
            .withProject(projectId)
            .withVarchar("key", buildVar.getKey()).build(), CommonUtil.getSingleString());
  
    // clone so that we don't make changes in supplied buildVar
    BuildVar clonedBuildVar = new BuildVar().setKey(buildVar.getKey()).setValue(buildVar.getValue())
        .setIsPrimary(buildVar.getIsPrimary());
  
    boolean resetCurrentPrimary = false;
    if (matchingKeys.size() == 1) {
      // key exist, set found key to clone so that we keep case wise same key in all vars in a group.
      clonedBuildVar.setKey(matchingKeys.get(0));
      if (clonedBuildVar.getIsPrimary()) {
        // must reset current primary as given var needs to be primary and key group already exists
        resetCurrentPrimary = true;
      }
    } else {
      // always make primary to a build var whose key doesn't yet exists
      clonedBuildVar.setIsPrimary(true);
    }
    
    String insertSql = "INSERT\n" +
        "INTO zwl_build_variables (key, value, isPrimary, bt_project_id)\n" +
        "VALUES (:key, :value, :isPrimary, :bt_project_id)\n" +
        "RETURNING zwl_build_variables_id";
    
    SqlParameterSource insertSqlParams = new SqlParamsBuilder()
        .withProject(projectId)
        .withVarchar("key", clonedBuildVar.getKey())
        .withOther("value", clonedBuildVar.getValue())
        .withBoolean("isPrimary", clonedBuildVar.getIsPrimary()).build();
  
    Supplier<Integer> insertOp = () ->
        jdbc.query(insertSql, insertSqlParams, CommonUtil.getSingleInt()).get(0);
    
    if (!resetCurrentPrimary) {
      return insertOp.get();
    }
    
    Integer result = transactionTemplate.execute(transactionStatus -> {
      // Throw runtime exceptions on problems, transaction will automatically rollbacks in case
      // of Runtime exception (default behavior). Note that all jdbcTemplate operations throw
      // a Runtime exception as well by translating SqlExceptions thus we don't have to worry
      // about any checked exceptions that don't rollback this transaction (default behavior)
      //assign resetting current primary sql
      String resetCurrentPrimarySql = "UPDATE zwl_build_variables SET isPrimary = false\n" +
          "WHERE key = :key AND isPrimary = true";
      SqlParameterSource resetCurrentPrimaryParams =
          new SqlParamsBuilder().withVarchar("key", clonedBuildVar.getKey()).build();
      int updateResult = jdbc.update(resetCurrentPrimarySql, resetCurrentPrimaryParams);
      if (updateResult != 1) {
        throw new RuntimeException("Couldn't reset current primary for key " +
            clonedBuildVar.getKey() + ", updated " + updateResult);
      }
      return insertOp.get();
    });
    Objects.requireNonNull(result);
    return result;
  }
  
  @Override
  public List<BuildVar> getBuildVars(int projectId, int userId, boolean onlyPrimary) {
    User user = common.getUserOwnProps(userId);
    StringBuilder sql = new StringBuilder("SELECT\n" +
        "b.zwl_build_variables_id, b.key, b.value, b.isPrimary FROM zwl_build_variables AS b\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_project_id = :bt_project_id AND p.organization_id = :organization_id");
    if (onlyPrimary) {
      sql.append("\nAND b.isPrimary = true");
    }
    
    SqlParameterSource namedParams = new SqlParamsBuilder()
        .withProject(projectId)
        .withOrganization(user.getOrganizationId())
        .build();
    
    return jdbc.query(sql.toString(), namedParams, (rs, rowNum) -> new BuildVar()
        .setId(rs.getInt("zwl_build_variables_id"))
        .setKey(rs.getString("key"))
        .setValue(rs.getString("value"))
        .setIsPrimary(rs.getBoolean("isPrimary")));
  }
  
  @Override
  public List<BuildVar> getPrimaryBuildVarsOverridingGiven(int projectId, int userId,
                                                           Map<String, Integer> overrideKeyId) {
    Preconditions.checkArgument(overrideKeyId.size() > 0, "overrideKeyId is empty");
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT\n" +
        "b.zwl_build_variables_id, b.key, b.value, b.isPrimary FROM zwl_build_variables AS b\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_project_id = :bt_project_id AND p.organization_id = :organization_id\n" +
        "AND b.isPrimary = true AND b.key NOT IN (SELECT * FROM unnest(:overrideKeys))\n" +
        "UNION ALL\n" +
        "SELECT\n" +
        "b.zwl_build_variables_id, b.key, b.value, b.isPrimary FROM zwl_build_variables AS b\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE p.organization_id = :organization_id\n" +
        "AND zwl_build_variables_id IN (SELECT * FROM unnest(:overrideIds))";
    SqlParameterSource namedParams = new SqlParamsBuilder()
        .withProject(projectId)
        .withOrganization(user.getOrganizationId())
        .withArray("overrideKeys", overrideKeyId.keySet().toArray(), JDBCType.VARCHAR)
        .withArray("overrideIds", overrideKeyId.values().toArray(), JDBCType.INTEGER).build();
    return jdbc.query(sql, namedParams, (rs, rowNum) -> new BuildVar()
        .setId(rs.getInt("zwl_build_variables_id"))
        .setKey(rs.getString("key"))
        .setValue(rs.getString("value"))
        .setIsPrimary(rs.getBoolean("isPrimary")));
  }
  
  @Override
  public void capturePrimaryBuildVarsOverridingGiven(int projectId,
                                                     Map<String, Integer> overrideKeyId,
                                                     int buildId) {
    String sql = "INSERT INTO bt_build_zwl_build_variables (bt_build_id, key, value)\n" +
        "SELECT :bt_build_id, key, value FROM zwl_build_variables\n" +
        "WHERE bt_project_id = :bt_project_id\n" +
        "AND isPrimary = true AND key NOT IN (SELECT * FROM unnest(:overrideKeys))\n" +
        "UNION ALL\n" +
        "SELECT :bt_build_id, key, value FROM zwl_build_variables\n" +
        "WHERE zwl_build_variables_id IN (SELECT * FROM unnest(:overrideIds))";
    SqlParameterSource namedParams = new SqlParamsBuilder()
        .withProject(projectId)
        .withInteger("bt_build_id", buildId)
        .withArray("overrideKeys", overrideKeyId.keySet().toArray(), JDBCType.VARCHAR)
        .withArray("overrideIds", overrideKeyId.values().toArray(), JDBCType.INTEGER).build();
    int result = jdbc.update(sql, namedParams);
    if (result < overrideKeyId.size()) {
      throw new RuntimeException("Couldn't insert build vars properly");
    }
  }
  
  @Override
  public void duplicateBuildVars(int duplicateBuildId, int originalBuildId) {
    String sql = "INSERT INTO bt_build_zwl_build_variables (bt_build_id, key, value)\n" +
        "SELECT :duplicate_build_id, key, value FROM bt_build_zwl_build_variables\n" +
        "WHERE bt_build_id = :original_build_id";
    SqlParameterSource namedParams = new SqlParamsBuilder()
        .withInteger("duplicate_build_id", duplicateBuildId)
        .withInteger("original_build_id", originalBuildId)
        .build();
    jdbc.update(sql, namedParams);
  }
  
  @Override
  public void updateBuildVar(String columnId, String value, int buildVarId, int projectId,
                            int userId) {
    User user = common.getUserOwnProps(userId);
    if (columnId.equals("value")) {
      String sql = "UPDATE zwl_build_variables AS b SET value = :value\n" +
          "FROM bt_project p\n" +
          "WHERE zwl_build_variables_id = :zwl_build_variables_id\n" +
          "AND b.bt_project_id = p.bt_project_id AND p.organization_id = :organization_id;";
      int result = jdbc.update(sql, new SqlParamsBuilder()
          .withOrganization(user.getOrganizationId())
          .withOther("value", value)
          .withInteger("zwl_build_variables_id", buildVarId).build());
      CommonUtil.validateSingleRowDbCommit(result);
    } else if (columnId.equals("isPrimary")) {
      if (!Boolean.parseBoolean(value)) {
        // a build var can be updated to primary only.
        throw new UnsupportedOperationException("Unexpectedly trying to un-primary a primary" +
            " build var");
      }
      common.verifyUsersProject(projectId, userId);
      transactionTemplate.executeWithoutResult(transactionStatus -> {
        SqlParameterSource namedParams = new SqlParamsBuilder()
            .withInteger("zwl_build_variables_id", buildVarId).build();
        String sql = "UPDATE zwl_build_variables SET isPrimary = false\n" +
            "WHERE key = (SELECT key from zwl_build_variables WHERE\n" +
            "zwl_build_variables_id = :zwl_build_variables_id)\n" +
            "AND isPrimary = true AND zwl_build_variables_id <> :zwl_build_variables_id;";
        // this update can fail if the variable we can making primary was already primary as query
        // won't find any primary record
        int updateResult = jdbc.update(sql, namedParams);
        if (updateResult != 1) {
          throw new RuntimeException("Couldn't reset current primary for key associated with" +
              " buildVarId " + buildVarId + ", updated " + updateResult);
        }
        sql = "UPDATE zwl_build_variables SET isPrimary = true\n" +
            "WHERE zwl_build_variables_id = :zwl_build_variables_id;";
        updateResult = jdbc.update(sql, namedParams);
        if (updateResult != 1) {
          throw new RuntimeException("Couldn't update to primary, updated " + updateResult);
        }
      });
    } else {
      throw new UnsupportedOperationException(columnId + " isn't allowed to be updated");
    }
  }
  
  @Override
  public void deleteBuildVar(int buildVarId, boolean isPrimary, int projectId, int userId) {
    User user = common.getUserOwnProps(userId);
    if (isPrimary) {
      String sql = "SELECT count(*) FROM zwl_build_variables\n" +
          "WHERE key = (SELECT key from zwl_build_variables WHERE\n" +
          "zwl_build_variables_id = :zwl_build_variables_id)\n" +
          "AND bt_project_id = :bt_project_id";
      int result = jdbc.query(sql, new SqlParamsBuilder()
          .withProject(projectId)
          .withInteger("zwl_build_variables_id", buildVarId).build(), CommonUtil.getSingleInt())
          .get(0);
      if (result > 1) {
        throw new RuntimeException("Can't delete a build var that is primary and key group" +
            " contains more than one var");
      }
    }
    String sql = "DELETE FROM zwl_build_variables b\n" +
        "USING bt_project p\n" +
        "WHERE zwl_build_variables_id = :zwl_build_variables_id\n" +
        "AND b.bt_project_id = p.bt_project_id AND p.organization_id = :organization_id";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withOrganization(user.getOrganizationId())
        .withInteger("zwl_build_variables_id", buildVarId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public List<CapturedVariable> getCapturedBuildVars(int buildId, int userId) {
    User user = common.getUserOwnProps(userId);
    String sql = "SELECT key, value FROM bt_build_zwl_build_variables AS bv\n" +
        "INNER JOIN bt_build AS b ON (bv.bt_build_id = b.bt_build_id)\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE bv.bt_build_id = :bt_build_id AND p.organization_id = :organization_id";
    return jdbc.query(sql, new SqlParamsBuilder()
            .withOrganization(user.getOrganizationId())
            .withInteger("bt_build_id", buildId).build(),
        (rs, rowNum) -> new CapturedVariable()
            .setKey(rs.getString("key"))
            .setValue(rs.getString("value")));
  }
}