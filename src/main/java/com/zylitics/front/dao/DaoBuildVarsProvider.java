package com.zylitics.front.dao;

import com.zylitics.front.model.BuildVars;
import com.zylitics.front.provider.BuildVarsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DaoBuildVarsProvider extends AbstractDaoProvider implements BuildVarsProvider {
  
  @Autowired
  DaoBuildVarsProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public List<BuildVars> getBuildVars(int projectId, int userId) {
    String sql = "SELECT\n" +
        "b.zwl_build_variables_id, b.key, b.value, b.isPrimary FROM zwl_build_variables AS b\n" +
        "INNER JOIN bt_project AS p ON (b.bt_project_id = p.bt_project_id)\n" +
        "WHERE b.bt_project_id = :bt_project_id AND p.zluser_id = :zluser_id;";
    
    SqlParameterSource namedParams = new SqlParamsBuilder(projectId, userId).build();
    
    return jdbc.query(sql, namedParams, (rs, rowNum) -> new BuildVars()
        .setId(rs.getInt("zwl_build_variables_id"))
        .setKey(rs.getString("key"))
        .setValue(rs.getString("value"))
        .setIsPrimary(rs.getBoolean("isPrimary")));
  }
}