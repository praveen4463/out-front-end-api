package com.zylitics.front.dao;

import com.zylitics.front.model.DurationUnit;
import com.zylitics.front.model.Plan;
import com.zylitics.front.model.PlanName;
import com.zylitics.front.model.PlanType;
import com.zylitics.front.provider.PlanProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoPlanProvider extends AbstractDaoProvider implements PlanProvider {
  
  @Autowired
  DaoPlanProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<Plan> getPlan(PlanName planName) {
    String sql = "SELECT p.plan_id, p.display_name, plan_type, minutes,\n" +
        "total_parallel, p.price, p.duration, p.duration_unit, s.name AS support_name\n" +
        "FROM plan AS p INNER JOIN support AS s ON (p.support_id = s.support_id)\n" +
        "WHERE p.name = :plan_name";
    List<Plan> plans  =  jdbc.query(sql, new SqlParamsBuilder()
        .withOther("plan_name", planName).build(), (rs, rowNum) ->
        new Plan()
            .setId(rs.getInt("plan_id"))
            .setName(planName)
            .setDisplayName(rs.getString("display_name"))
            .setPlanType(PlanType.valueOf(rs.getString("plan_type")))
            .setMinutes(rs.getInt("minutes"))
            .setTotalParallel(rs.getInt("total_parallel"))
            .setPrice(rs.getLong("price"))
            .setDuration(rs.getInt("duration"))
            .setDurationUnit(DurationUnit.valueOf(rs.getString("duration_unit")))
            .setSupportName(rs.getString("support_name")));
    if (plans.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(plans.get(0));
  }
}
