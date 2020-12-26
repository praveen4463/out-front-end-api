package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.BuildVar;
import com.zylitics.front.provider.BuildVarProvider;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
  public Optional<Integer> newBuildVar(BuildVar buildVar, int projectId, int userId) {
    Preconditions.checkNotNull(buildVar, "buildVar is required");
    
    common.verifyUsersProject(projectId, userId); // check access initially as there are several
    // queries here and I don't want a join on all
    
    // check dupe build var
    String checkExistenceSql = "SELECT count(*) FROM zwl_build_variables\n" +
        "WHERE key ILIKE lower(:key) AND value = :value AND bt_project_id = :bt_project_id;";
    Integer matchingVar = jdbc.queryForObject(checkExistenceSql,
        new SqlParamsBuilder()
            .withProject(projectId)
            .withVarchar("key", buildVar.getKey())
            .withOther("value", buildVar.getValue()).build(),
        Integer.class);
    Objects.requireNonNull(matchingVar);
    if (matchingVar > 0) {
      throw new IllegalArgumentException("Can't add build variable, given key and value already" +
          " exists");
    }
  
    // check whether given build var's key already exists
    String checkKeySql = "SELECT key FROM zwl_build_variables\n" +
        "WHERE bt_project_id = :bt_project_id AND key ILIKE lower(:key) group by key;";
    List<String> matchingKeys = jdbc.query(checkKeySql,
        new SqlParamsBuilder()
            .withProject(projectId)
            .withVarchar("key", buildVar.getKey()).build(), (rs, rowNum) -> rs.getString("key"));
  
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
        "RETURNING zwl_build_variables_id;";
    
    SqlParameterSource insertSqlParams = new SqlParamsBuilder()
        .withProject(projectId)
        .withVarchar("key", clonedBuildVar.getKey())
        .withOther("value", clonedBuildVar.getValue())
        .withBoolean("isPrimary", clonedBuildVar.getIsPrimary()).build();
  
    Supplier<Optional<Integer>> insertOp = () ->
        Optional.ofNullable(jdbc.queryForObject(insertSql, insertSqlParams, Integer.class));
    
    if (!resetCurrentPrimary) {
      return insertOp.get();
    }
    
    return transactionTemplate.execute(transactionStatus -> {
      // Throw runtime exceptions on problems, transaction will automatically rollbacks in case
      // of Runtime exception (default behavior). Note that all jdbcTemplate operations throw
      // a Runtime exception as well by translating SqlExceptions thus we don't have to worry
      // about any checked exceptions that don't rollback this transaction (default behavior)
      //assign resetting current primary sql
      String resetCurrentPrimarySql = "UPDATE zwl_build_variables SET isPrimary = false\n" +
          "WHERE key = :key AND isPrimary = true;";
      SqlParameterSource resetCurrentPrimaryParams =
          new SqlParamsBuilder().withVarchar("key", clonedBuildVar.getKey()).build();
      int updateResult = jdbc.update(resetCurrentPrimarySql, resetCurrentPrimaryParams);
      if (updateResult != 1) {
        throw new RuntimeException("Couldn't reset current primary for key " +
            clonedBuildVar.getKey() + ", updated " + updateResult);
      }
      Optional<Integer> insertResult = insertOp.get();
      // checking here cause we need to rollback if not inserted.
      if (!insertResult.isPresent()) {
        throw new RuntimeException("Insert failed for key " + clonedBuildVar.getKey());
      }
      return insertResult;
    });
  }
  
  @Override
  public List<BuildVar> getBuildVars(int projectId, int userId) {
    String sql = "SELECT\n" +
        "b.zwl_build_variables_id, b.key, b.value, b.isPrimary FROM zwl_build_variables AS b\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_project_id = :bt_project_id AND p.zluser_id = :zluser_id;";
    
    SqlParameterSource namedParams = new SqlParamsBuilder(projectId, userId).build();
    
    return jdbc.query(sql, namedParams, (rs, rowNum) -> new BuildVar()
        .setId(rs.getInt("zwl_build_variables_id"))
        .setKey(rs.getString("key"))
        .setValue(rs.getString("value"))
        .setIsPrimary(rs.getBoolean("isPrimary")));
  }
  
  @Override
  public int updateBuildVar(String columnId, String value, int buildVarId, int projectId,
                            int userId) {
    if (columnId.equals("value")) {
      String sql = "UPDATE zwl_build_variables AS b SET value = :value\n" +
          "FROM bt_project p\n" +
          "WHERE zwl_build_variables_id = :zwl_build_variables_id\n" +
          "AND b.bt_project_id = p.bt_project_id AND p.zluser_id = :zluser_id;";
      return jdbc.update(sql, new SqlParamsBuilder(userId)
          .withOther("value", value)
          .withInteger("zwl_build_variables_id", buildVarId).build());
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
      return 1;
    }
    throw new UnsupportedOperationException(columnId + " isn't allowed to be updated");
  }
  
  @Override
  public int deleteBuildVar(int buildVarId, boolean isPrimary, int userId) {
    if (isPrimary) {
      String sql = "SELECT count(*) FROM zwl_build_variables\n" +
          "WHERE key = (SELECT key from zwl_build_variables WHERE\n" +
          "zwl_build_variables_id = :zwl_build_variables_id)\n";
      Integer result = jdbc.queryForObject(sql, new SqlParamsBuilder()
          .withInteger("zwl_build_variables_id", buildVarId).build(), Integer.class);
      Objects.requireNonNull(result);
      if (result > 1) {
        throw new RuntimeException("Can't delete a build var that is primary and key group" +
            " contains more than one var");
      }
    }
    String sql = "DELETE FROM zwl_build_variables b\n" +
        "USING bt_project p\n" +
        "WHERE zwl_build_variables_id = :zwl_build_variables_id\n" +
        "AND b.bt_project_id = p.bt_project_id AND p.zluser_id = :zluser_id";
    return jdbc.update(sql, new SqlParamsBuilder(userId)
        .withInteger("zwl_build_variables_id", buildVarId).build());
  }
}