package com.zylitics.front.dao;

import com.zylitics.front.model.Plan;
import com.zylitics.front.model.PlanName;
import com.zylitics.front.provider.PlanProvider;
import com.zylitics.front.provider.QuotaProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.function.BiFunction;

@Repository
public class DaoQuotaProvider extends AbstractDaoProvider implements QuotaProvider {
  
  private final PlanProvider planProvider;
  
  @Autowired
  DaoQuotaProvider(NamedParameterJdbcTemplate jdbc, PlanProvider planProvider) {
    super(jdbc);
    this.planProvider = planProvider;
  }
  
  @Override
  public void newQuota(PlanName planName, int organizationId) {
    Plan plan = planProvider.getPlan(planName)
        .orElseThrow(() -> new RuntimeException("Given planName doesn't exist: " + planName));
    BiFunction<OffsetDateTime, Integer, OffsetDateTime> getPlannedDate;
    switch (plan.getDurationUnit()) {
      case SECONDS:
        getPlannedDate = OffsetDateTime::plusSeconds;
        break;
      case MINUTES:
        getPlannedDate = OffsetDateTime::plusMinutes;
        break;
      case HOURS:
        getPlannedDate = OffsetDateTime::plusHours;
        break;
      case DAYS:
        getPlannedDate = OffsetDateTime::plusDays;
        break;
      case MONTHS:
        getPlannedDate = OffsetDateTime::plusMonths;
        break;
      case YEARS:
        getPlannedDate = OffsetDateTime::plusYears;
        break;
      default:
        throw new RuntimeException("Couldn't identify duration unit " + plan.getDurationUnit());
    }
    String sql = "INSERT INTO quota (plan_id, billing_cycle_start, billing_cycle_planned_end,\n" +
        "organization_id)\n" +
        "VALUES (:plan_id, :billing_cycle_start, :billing_cycle_planned_end, :organization_id)";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withInteger("plan_id", plan.getId())
        .withTimestampTimezone("billing_cycle_start", OffsetDateTime.now())
        .withTimestampTimezone("billing_cycle_planned_end",
            getPlannedDate.apply(OffsetDateTime.now(), plan.getDuration()))
        .withInteger("organization_id", organizationId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
