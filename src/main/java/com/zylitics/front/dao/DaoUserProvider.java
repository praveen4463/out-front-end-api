package com.zylitics.front.dao;

import com.google.common.base.Strings;
import com.zylitics.front.model.*;
import com.zylitics.front.provider.EmailPreferenceProvider;
import com.zylitics.front.provider.OrganizationProvider;
import com.zylitics.front.provider.QuotaProvider;
import com.zylitics.front.provider.UserProvider;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.front.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class DaoUserProvider extends AbstractDaoProvider implements UserProvider {
  
  private final TransactionTemplate transactionTemplate;
  
  private final QuotaProvider quotaProvider;
  
  private final OrganizationProvider organizationProvider;
  
  private final EmailPreferenceProvider emailPreferenceProvider;
  
  @Autowired
  DaoUserProvider(NamedParameterJdbcTemplate jdbc,
                  TransactionTemplate transactionTemplate,
                  QuotaProvider quotaProvider,
                  OrganizationProvider organizationProvider,
                  EmailPreferenceProvider emailPreferenceProvider) {
    super(jdbc);
    this.transactionTemplate = transactionTemplate;
    this.quotaProvider = quotaProvider;
    this.organizationProvider = organizationProvider;
    this.emailPreferenceProvider = emailPreferenceProvider;
  }
  
  @Override
  public User newUser(NewUser newUser) {
    Integer userId = transactionTemplate.execute(ts -> {
      int organizationId;
      PlanName planName = null;
      Class<?> userClass = newUser.getClass();
      if (userClass == NewUserInOrganization.class) {
        organizationId = ((NewUserInOrganization) newUser).getOrganizationId();
      } else if (userClass == NewUserNewOrganization.class) {
        // create new organization
        NewUserNewOrganization newUserNewOrganization = (NewUserNewOrganization) newUser;
        organizationId = organizationProvider
            .newOrganization(newUserNewOrganization.getOrganizationName());
        planName = newUserNewOrganization.getPlanName();
      } else {
        throw new RuntimeException("No suitable overriding class found for: " +
            userClass.getSimpleName());
      }
      // now we've organizationId either given by user or created new. Create user record
      String sql = "INSERT INTO zluser (first_name, last_name, email, timezone, role,\n" +
          "organization_id, shot_bucket_session_storage, email_verification_id, create_date)\n" +
          "VALUES (:first_name, :last_name, :email, :timezone, :role, :organization_id,\n" +
          ":shot_bucket_session_storage, :email_verification_id, :create_date)\n" +
          "RETURNING zluser_id";
      int id = jdbc.query(sql, new SqlParamsBuilder()
          .withOther("first_name", newUser.getFirstName())
          .withOther("last_name", newUser.getLastName())
          .withOther("email", newUser.getEmail())
          .withOther("timezone", newUser.getTimezone())
          .withOther("role", newUser.getRole())
          .withInteger("organization_id", organizationId)
          .withOther("shot_bucket_session_storage", newUser.getShotBucketSessionStorage())
          .withBigint("email_verification_id", newUser.getEmailVerificationId())
          .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
      // if a planName was given, we need to create new quota as this is a new organization
      if (planName != null) {
        quotaProvider.newQuota(planName, organizationId);
      }
      
      // Create mandatory records for this user
      emailPreferenceProvider.newEmailPreference(id);
      
      return id;
    });
    Objects.requireNonNull(userId);
    return getUser(userId).orElseThrow(() -> new RuntimeException("Couldn't get user " + userId));
  }
  
  @Override
  public Optional<User> getUser(int userId) {
    return getUser(userId, false);
  }
  
  @Override
  public Optional<User> getUser(int userId, boolean ownDetailsOnly) {
    String sql = "SELECT first_name, last_name, email, timezone, role, u.organization_id,\n" +
        "shot_bucket_session_storage, email_verification_id\n";
    
    if (ownDetailsOnly) {
      sql += "FROM zluser AS u WHERE zluser_id = :zluser_id";
    } else {
      sql += ", o.name AS org_name, api_key, p.name AS plan_name, plan_type, p.display_name,\n" +
          "minutes, total_parallel, coalesce(minutes_consumed, 0) AS minutes_consumed,\n" +
          "billing_cycle_start AT TIME ZONE 'UTC' AS billing_cycle_start,\n" +
          "billing_cycle_planned_end AT TIME ZONE 'UTC' AS billing_cycle_planned_end\n" +
          "FROM zluser AS u\n" +
          "INNER JOIN organization AS o ON (u.organization_id = o.organization_id)\n" +
          "INNER JOIN quota AS q ON (o.organization_id = q.organization_id)\n" +
          "INNER JOIN plan AS p ON (q.plan_id = p.plan_id)\n" +
          "WHERE u.zluser_id = :zluser_id AND q.billing_cycle_actual_end IS NULL";
    }
    List<User> user = jdbc.query(sql, new SqlParamsBuilder(userId).build(), (rs, rowNum) -> {
        User u = new User()
            .setId(userId)
            .setFirstName(rs.getString("first_name"))
            .setLastName(rs.getString("last_name"))
            .setEmail(rs.getString("email"))
            .setTimezone(rs.getString("timezone"))
            .setRole(Role.valueOf(rs.getString("role")))
            .setOrganizationId(rs.getInt("organization_id"))
            .setShotBucketSessionStorage(rs.getString("shot_bucket_session_storage"))
            .setEmailVerificationId(rs.getLong("email_verification_id"));
        if (!ownDetailsOnly) {
          u.setOrganization(new Organization()
                .setName(rs.getString("org_name"))
                .setApiKey(rs.getString("api_key")))
            .setUsersPlan(new UsersPlan()
                .setPlanName(rs.getString("plan_name"))
                .setPlanType(PlanType.valueOf(rs.getString("plan_type")))
                .setDisplayName(rs.getString("display_name"))
                .setTotalMinutes(rs.getInt("minutes"))
                .setTotalParallel(rs.getInt("total_parallel"))
                .setConsumedMinutes(rs.getInt("minutes_consumed"))
                .setBillingCycleStart(
                    DateTimeUtil.sqlUTCTimestampToEpochSecs(rs.getTimestamp("billing_cycle_start")))
                .setBillingCyclePlannedEnd(DateTimeUtil.sqlUTCTimestampToEpochSecs(
                    rs.getTimestamp("billing_cycle_planned_end")))
            );
        }
        return u;
    });
    
    if (user.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(user.get(0));
  }
  
  @Override
  public boolean userWithEmailExist(String email) {
    String sql = "SELECT count(*) FROM zluser WHERE email ILIKE lower(:email)";
    int result = jdbc.query(sql, new SqlParamsBuilder().withOther("email", email).build(),
        CommonUtil.getSingleInt()).get(0);
    return result > 0;
  }
  
  @Override
  public Optional<Integer> getUserId(String email) {
    String sql = "SELECT zluser_id FROM zluser WHERE email ILIKE lower(:email)";
    List<Integer> ids = jdbc.query(sql, new SqlParamsBuilder().withOther("email", email).build(),
        CommonUtil.getSingleInt());
    if (ids.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(ids.get(0));
  }
  
  @Override
  public String getUserEmail(int userId) {
    String sql = "SELECT email FROM zluser WHERE zluser_id = :zluser_id";
    return jdbc.query(sql, new SqlParamsBuilder(userId).build(),
        CommonUtil.getSingleString()).get(0);
  }
  
  @Override
  public void updateEmail(int userId, String newEmail) {
    String sql = "UPDATE zluser SET email = :email WHERE zluser_id = :zluser_id";
    int result = jdbc.update(sql, new SqlParamsBuilder(userId)
        .withOther("email", newEmail).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public void updateProfile(int userId, UserUpdatableProfile userUpdatableProfile) {
    transactionTemplate.executeWithoutResult(ts -> {
      boolean somethingUpdated = false;
      String comma = ",";
      String commaNewLine = comma + "\n";
      // first things that are in zluser
      StringBuilder sqlBuilder = new StringBuilder();
      SqlParamsBuilder sqlParamsBuilder = new SqlParamsBuilder(userId);
      if (!Strings.isNullOrEmpty(userUpdatableProfile.getFirstName())) {
        sqlBuilder.append("first_name = :first_name");
        sqlBuilder.append(commaNewLine);
        sqlParamsBuilder.withOther("first_name", userUpdatableProfile.getFirstName());
      }
      if (!Strings.isNullOrEmpty(userUpdatableProfile.getLastName())) {
        sqlBuilder.append("last_name = :last_name");
        sqlBuilder.append(commaNewLine);
        sqlParamsBuilder.withOther("last_name", userUpdatableProfile.getLastName());
      }
      if (!Strings.isNullOrEmpty(userUpdatableProfile.getTimezone())) {
        sqlBuilder.append("timezone = :timezone");
        sqlBuilder.append(commaNewLine);
        sqlParamsBuilder.withOther("timezone", userUpdatableProfile.getTimezone());
      }
      if (sqlBuilder.length() > 0) {
        String sql = sqlBuilder.toString();
        sql = sql.substring(0, sql.lastIndexOf(comma)); // remove the trailing comma
        int result = jdbc.update(
            String.format("UPDATE zluser SET\n%s\nWHERE zluser_id = :zluser_id", sql),
            sqlParamsBuilder.build());
        CommonUtil.validateSingleRowDbCommit(result);
        somethingUpdated = true;
      }
      
      // update organization
      if (!Strings.isNullOrEmpty(userUpdatableProfile.getOrganizationName())) {
        organizationProvider.updateOrganizationName(userId,
            userUpdatableProfile.getOrganizationName());
        somethingUpdated = true;
      }
      if (!somethingUpdated) {
        throw new IllegalArgumentException("Nothing given to update user profile");
      }
    });
  }
}
