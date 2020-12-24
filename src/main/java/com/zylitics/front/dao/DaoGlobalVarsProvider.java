package com.zylitics.front.dao;

import com.zylitics.front.model.GlobalVars;
import com.zylitics.front.provider.GlobalVarsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DaoGlobalVarsProvider extends AbstractDaoProvider implements GlobalVarsProvider {
  
  @Autowired
  DaoGlobalVarsProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public List<GlobalVars> getGlobalVars(int projectId, int userId) {
    String sql = "SELECT g.zwl_globals_id, g.key, g.value FROM zwl_globals AS g\n" +
        "INNER JOIN bt_project AS p ON (g.bt_project_id = p.bt_project_id)\n" +
        "WHERE g.bt_project_id = :bt_project_id AND p.zluser_id = :zluser_id;";
  
    SqlParameterSource namedParams = new SqlParamsBuilder(projectId, userId).build();
    
    return jdbc.query(sql, namedParams, (rs, rowNum) -> new GlobalVars()
        .setId(rs.getInt("zwl_globals_id"))
        .setKey(rs.getString("key"))
        .setValue(rs.getString("value")));
  }
}
