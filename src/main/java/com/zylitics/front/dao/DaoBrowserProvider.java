package com.zylitics.front.dao;

import com.zylitics.front.model.BrowserNameToVersions;
import com.zylitics.front.provider.BrowserProvider;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DaoBrowserProvider extends AbstractDaoProvider implements BrowserProvider {
  
  DaoBrowserProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public List<BrowserNameToVersions> getBrowsers(String platform) {
    String sql = "SELECT name, array_agg(display_version order by display_version) AS versions\n" +
        "FROM bt_browser_fe WHERE :platform = ANY (platforms) AND deprecated = false\n" +
        "GROUP BY name ORDER BY name";
    return jdbc.query(sql, new SqlParamsBuilder().withOther("platform", platform).build(),
        (rs, rowNum) -> new BrowserNameToVersions()
            .setName(rs.getString("name"))
            .setVersions((String[]) rs.getArray("versions").getArray()));
  }
}
