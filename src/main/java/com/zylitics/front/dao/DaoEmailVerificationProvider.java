package com.zylitics.front.dao;

import com.zylitics.front.model.EmailVerification;
import com.zylitics.front.model.EmailVerificationUserType;
import com.zylitics.front.model.NewEmailVerification;
import com.zylitics.front.model.Role;
import com.zylitics.front.provider.EmailVerificationProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoEmailVerificationProvider extends AbstractDaoProvider
    implements EmailVerificationProvider {
  
  public DaoEmailVerificationProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void newEmailVerification(NewEmailVerification newEmailVerification) {
    String sql = "INSERT INTO email_verification (email, code, email_verification_user_type,\n" +
        "role, organization_id, create_date)\n" +
        "VALUES (:email, :code, :email_verification_user_type, :role, :organization_id,\n" +
        ":create_date)";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withOther("email", newEmailVerification.getEmail())
        .withVarchar("code", newEmailVerification.getCode())
        .withOther("email_verification_user_type",
            newEmailVerification.getEmailVerificationUserType())
        .withOther("role", newEmailVerification.getRole())
        .withInteger("organization_id", newEmailVerification.getOrganizationId())
        .withCreateDate().build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public Optional<EmailVerification> getEmailVerification(String code) {
    return getEmailVerification("code = :code",
        new SqlParamsBuilder().withVarchar("code", code).build());
  }
  
  @Override
  public Optional<EmailVerification> getEmailVerification(long id) {
    return getEmailVerification("email_verification_id = :email_verification_id",
        new SqlParamsBuilder().withBigint("email_verification_id", id).build());
  }
  
  private Optional<EmailVerification> getEmailVerification(String postWhereSql,
                                                           SqlParameterSource sqlParameterSource) {
    String sql = "SELECT email_verification_id, email, code, used,\n" +
        "email_verification_user_type, role, organization_id\n" +
        "FROM email_verification WHERE " + postWhereSql;
    List<EmailVerification> emailVerifications = jdbc.query(sql, sqlParameterSource,
        (rs, rowNum) -> new EmailVerification()
            .setId(rs.getInt("email_verification_id"))
            .setEmail(rs.getString("email"))
            .setCode(rs.getString("code"))
            .setUsed(rs.getBoolean("used"))
            .setEmailVerificationUserType(
                EmailVerificationUserType.valueOf(rs.getString("email_verification_user_type")))
            .setRole(rs.getString("role") != null ? Role.valueOf(rs.getString("role")) : null)
            // get as object and cast because we need it null if its sql null rather than int 0
            .setOrganizationId((Integer) rs.getObject("organization_id")));
    if (emailVerifications.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(emailVerifications.get(0));
  }
  
  @Override
  public void updateToUsed(long id) {
    String sql = "UPDATE email_verification SET used = true\n" +
        "WHERE email_verification_id = :email_verification_id";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withBigint("email_verification_id", id).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
