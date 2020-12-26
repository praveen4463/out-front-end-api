package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.GlobalVar;
import com.zylitics.front.provider.GlobalVarProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class DaoGlobalVarProvider extends AbstractDaoProvider implements GlobalVarProvider {
  
  @Autowired
  DaoGlobalVarProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<Integer> newGlobalVar(GlobalVar globalVar, int projectId, int userId) {
    Preconditions.checkNotNull(globalVar, "globalVar can't be null");
  
    // first check whether there is a duplicate
    String sql = "SELECT count(*) FROM zwl_globals g\n" +
        "INNER JOIN bt_project AS p ON (g.bt_project_id = p.bt_project_id)\n" +
        "WHERE g.bt_project_id = :bt_project_id AND p.zluser_id = :zluser_id\n" +
        "AND g.key ILIKE lower(:key);";
    SqlParameterSource namedParams = new SqlParamsBuilder(projectId, userId)
        .withVarchar("key", globalVar.getKey()).build();
    Integer matching = jdbc.queryForObject(sql, namedParams, Integer.class);
    Objects.requireNonNull(matching);
    if (matching > 0) {
      throw new IllegalArgumentException("A global variable with the same name already exists");
    }
    
    // make sure user owns the projectId
    sql = "INSERT INTO zwl_globals (key, value, bt_project_id)\n" +
        "SELECT :key, :value, :bt_project_id FROM bt_project\n" +
        "WHERE bt_project_id = :bt_project_id AND zluser_id = :zluser_id\n" +
        "RETURNING zwl_globals_id;";
    SqlParameterSource namedParamsInsert = new SqlParamsBuilder(projectId, userId)
        .withVarchar("key", globalVar.getKey())
        .withOther("value", globalVar.getValue()).build();
    return Optional.ofNullable(jdbc.queryForObject(sql, namedParamsInsert, Integer.class));
  }
  
  @Override
  public List<GlobalVar> getGlobalVars(int projectId, int userId) {
    String sql = "SELECT g.zwl_globals_id, g.key, g.value FROM zwl_globals AS g\n" +
        "INNER JOIN bt_project AS p ON (g.bt_project_id = p.bt_project_id)\n" +
        "WHERE g.bt_project_id = :bt_project_id AND p.zluser_id = :zluser_id;";
  
    SqlParameterSource namedParams = new SqlParamsBuilder(projectId, userId).build();
    
    return jdbc.query(sql, namedParams, (rs, rowNum) -> new GlobalVar()
        .setId(rs.getInt("zwl_globals_id"))
        .setKey(rs.getString("key"))
        .setValue(rs.getString("value")));
  }
  
  @Override
  public int updateValue(String value, int globalVarId, int userId) {
    String sql = "UPDATE zwl_globals AS g SET value = :value\n" +
        "FROM bt_project AS p WHERE zwl_globals_id = :zwl_globals_id\n" +
        "AND g.bt_project_id = p.bt_project_id AND p.zluser_id = :zluser_id;";
    return jdbc.update(sql, new SqlParamsBuilder(userId)
        .withOther("value", value)
        .withInteger("zwl_globals_id", globalVarId).build());
  }
  
  @Override
  public int deleteGlobalVar(int globalVarId, int userId) {
    Preconditions.checkArgument(globalVarId > 0, "globalVarId is required");
    String sql = "DELETE FROM zwl_globals AS g\n" +
        "USING bt_project AS p WHERE zwl_globals_id = :zwl_globals_id\n" +
        "AND g.bt_project_id = p.bt_project_id AND p.zluser_id = :zluser_id;";
    return jdbc.update(sql, new SqlParamsBuilder(userId)
        .withInteger("zwl_globals_id", globalVarId).build());
  }
}
