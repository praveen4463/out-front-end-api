package com.zylitics.front.dao;

import com.zylitics.front.model.BrowserNameToVersions;
import com.zylitics.front.provider.BrowserProvider;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.List;

@Repository
public class DaoBrowserProvider extends AbstractDaoProvider implements BrowserProvider {
  
  DaoBrowserProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public List<BrowserNameToVersions> getBrowsers(@Nullable String platform) {
    SqlParamsBuilder paramsBuilder = new SqlParamsBuilder();
    String sql = "SELECT name, array_agg(display_version order by display_version) AS versions\n" +
        "FROM bt_browser_fe WHERE deprecated = false\n";
    if (platform != null) {
      sql += "AND :platform = ANY (platforms)\n";
      paramsBuilder.withOther("platform", platform);
    }
    sql += "GROUP BY name ORDER BY name";
    return jdbc.query(sql, paramsBuilder.build(),
        (rs, rowNum) -> new BrowserNameToVersions()
            .setName(rs.getString("name"))
            .setVersions((String[]) rs.getArray("versions").getArray()));
  }
}
