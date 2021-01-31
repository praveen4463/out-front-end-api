package com.zylitics.front.dao;

import com.zylitics.front.model.*;
import com.zylitics.front.provider.OrganizationProvider;
import com.zylitics.front.provider.QuotaProvider;
import com.zylitics.front.provider.UserProvider;
import com.zylitics.front.util.CommonUtil;
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
  
  @Autowired
  DaoUserProvider(NamedParameterJdbcTemplate jdbc,
                  TransactionTemplate transactionTemplate,
                  QuotaProvider quotaProvider,
                  OrganizationProvider organizationProvider) {
    super(jdbc);
    this.transactionTemplate = transactionTemplate;
    this.quotaProvider = quotaProvider;
    this.organizationProvider = organizationProvider;
  }
  
  @Override
  public User newUser(NewUser newUser) {
    User user = transactionTemplate.execute(ts -> {
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
          "VALUES (:first_name, :last_name, :email, :timezone, :role, :organization_id)\n" +
          ":shot_bucket_session_storage, :email_verification_id, :create_date)\n" +
          "RETURNING zluser_id";
      int userId = jdbc.query(sql, new SqlParamsBuilder()
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
      return new User()
          .setId(userId)
          .setFirstName(newUser.getFirstName())
          .setLastName(newUser.getLastName())
          .setEmail(newUser.getEmail())
          .setTimezone(newUser.getTimezone())
          .setRole(newUser.getRole())
          .setOrganizationId(organizationId)
          .setShotBucketSessionStorage(newUser.getShotBucketSessionStorage())
          .setEmailVerificationId(newUser.getEmailVerificationId());
    });
    Objects.requireNonNull(user);
    return user;
  }
  
  @Override
  public Optional<User> getUserWithPlan(int userId) {
    String sql = "SELECT first_name, last_name, email, timezone, role, u.organization_id,\n" +
        "shot_bucket_session_storage, email_verification_id, plan_type, minutes,\n" +
        "total_parallel, coalesce(minutes_consumed, 0) AS minutes_consumed\n" +
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
            .setRole(Role.valueOf(rs.getString("role")))
            .setOrganizationId(rs.getInt("organization_id"))
            .setShotBucketSessionStorage(rs.getString("shot_bucket_session_storage"))
            .setEmailVerificationId(rs.getLong("email_verification_id"))
            .setUsersPlan(new UsersPlan()
                .setPlanType(PlanType.valueOf(rs.getString("plan_type")))
                .setTotalMinutes(rs.getInt("minutes"))
                .setTotalParallel(rs.getInt("total_parallel"))
                .setConsumedMinutes(rs.getInt("minutes_consumed"))));
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
}
