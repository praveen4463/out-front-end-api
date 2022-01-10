package com.zylitics.front.dao;

import com.zylitics.front.provider.EmailPreferenceProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DaoEmailPreferenceProvider extends AbstractDaoProvider
    implements EmailPreferenceProvider {
  
  DaoEmailPreferenceProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void newEmailPreference(int userId) {
    String sql = "INSERT into email_preference(zluser_id)\n" +
        "VALUES (:zluser_id)";
    int result = jdbc.update(sql, new SqlParamsBuilder(userId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
