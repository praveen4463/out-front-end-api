package com.zylitics.front.dao;

import com.zylitics.front.model.*;
import com.zylitics.front.provider.PasswordResetProvider;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.front.util.DateTimeUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoPasswordResetProvider extends AbstractDaoProvider implements PasswordResetProvider {
  
  public DaoPasswordResetProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void newPasswordReset(NewPasswordReset newPasswordReset) {
    String sql = "INSERT INTO password_reset (code, zluser_id, create_date)\n" +
        "VALUES (:code, :zluser_id, :create_date)";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withVarchar("code", newPasswordReset.getCode())
        .withInteger("zluser_id", newPasswordReset.getUserId())
        .withCreateDate().build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public Optional<PasswordReset> getPasswordReset(String code) {
    return getPasswordReset("code = :code",
        new SqlParamsBuilder().withVarchar("code", code).build());
  }
  
  @Override
  public Optional<PasswordReset> getPasswordReset(long id) {
    return getPasswordReset("password_reset_id = :password_reset_id",
        new SqlParamsBuilder().withBigint("password_reset_id", id).build());
  }
  
  private Optional<PasswordReset> getPasswordReset(String postWhereSql,
                                                   SqlParameterSource sqlParameterSource) {
    String sql = "SELECT password_reset_id, code, used, zluser_id, create_date\n" +
        "FROM password_reset WHERE " + postWhereSql;
    List<PasswordReset> passwordResets = jdbc.query(sql, sqlParameterSource,
        (rs, rowNum) -> new PasswordReset()
            .setId(rs.getInt("password_reset_id"))
            .setCode(rs.getString("code"))
            .setUsed(rs.getBoolean("used"))
            .setUserId(rs.getInt("zluser_id"))
            .setCreateDate(DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp("create_date"))));
    if (passwordResets.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(passwordResets.get(0));
  }
  
  @Override
  public void updateToUsed(long id) {
    String sql = "UPDATE password_reset SET used = true\n" +
        "WHERE password_reset_id = :password_reset_id";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withBigint("password_reset_id", id).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
