package com.zylitics.front.dao;

import com.zylitics.front.model.PlanType;
import com.zylitics.front.model.User;
import com.zylitics.front.provider.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoUserProvider extends AbstractDaoProvider implements UserProvider {
  
  @Autowired
  DaoUserProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<User> getUser(int userId) {
    String sql = "SELECT first_name, last_name, email, timezone, role,\n" +
        "shot_bucket_session_storage, plan_type, minutes, total_parallel,\n" +
        "coalesce(minutes_consumed, 0) AS minutes_consumed\n" +
        "FROM zluser AS u\n" +
        "INNER JOIN quota AS q ON (u.organization_id = q.organization_id)\n" +
        "INNER JOIN plan AS p ON (q.plan_id = p.plan_id)\n" +
        "WHERE u.zluser_id = :zluser_id AND q.billing_cycle_actual_end IS NULL";
    List<User> user = jdbc.query(sql, new SqlParamsBuilder(userId).build(), (rs, rowNum) ->
        new User()
            .setId(userId)
            .setFirstName(rs.getString("first_name"))
            .setLastName(rs.getString("last_name"))
            .setEmail(rs.getString("email"))
            .setTimezone(rs.getString("timezone"))
            .setRole(rs.getString("role"))
            .setShotBucketSessionStorage(rs.getString("shot_bucket_session_storage"))
            .setPlanType(PlanType.fromDbName(rs.getString("plan_type")))
            .setTotalMinutes(rs.getInt("minutes"))
            .setTotalParallel(rs.getInt("total_parallel"))
            .setConsumedMinutes(rs.getInt("minutes_consumed")));
    if (user.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(user.get(0));
  }
}
