package com.zylitics.front.dao;

import com.zylitics.front.model.EmailChange;
import com.zylitics.front.model.NewEmailChange;
import com.zylitics.front.provider.EmailChangeProvider;
import com.zylitics.front.util.CommonUtil;
import com.zylitics.front.util.DateTimeUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoEmailChangeProvider extends AbstractDaoProvider implements EmailChangeProvider {
  
  public DaoEmailChangeProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public void newEmailChange(NewEmailChange newEmailChange) {
    String sql = "INSERT INTO email_change (previous_email, new_email, code, zluser_id,\n" +
        "create_date)\n" +
        "VALUES (:previous_email, :new_email, :code, :zluser_id, :create_date)";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withOther("previous_email", newEmailChange.getPreviousEmail())
        .withOther("new_email", newEmailChange.getNewEmail())
        .withVarchar("code", newEmailChange.getCode())
        .withInteger("zluser_id", newEmailChange.getUserId())
        .withCreateDate().build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public Optional<EmailChange> getEmailChange(String code) {
    return getEmailChange("code = :code",
        new SqlParamsBuilder().withVarchar("code", code).build());
  }
  
  @Override
  public Optional<EmailChange> getEmailChange(long id) {
    return getEmailChange("email_change_id = :email_change_id",
        new SqlParamsBuilder().withBigint("email_change_id", id).build());
  }
  
  private Optional<EmailChange> getEmailChange(String postWhereSql,
                                               SqlParameterSource sqlParameterSource) {
    String sql = "SELECT email_change_id, previous_email, new_email, code, used, zluser_id," +
        "create_date\n" +
        "FROM email_change WHERE " + postWhereSql;
    List<EmailChange> emailChangeList = jdbc.query(sql, sqlParameterSource,
        (rs, rowNum) -> new EmailChange()
            .setId(rs.getInt("email_change_id"))
            .setPreviousEmail(rs.getString("previous_email"))
            .setNewEmail(rs.getString("new_email"))
            .setCode(rs.getString("code"))
            .setUsed(rs.getBoolean("used"))
            .setUserId(rs.getInt("zluser_id"))
            .setCreateDate(DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp("create_date"))));
    if (emailChangeList.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(emailChangeList.get(0));
  }
  
  @Override
  public void updateToUsed(long id) {
    String sql = "UPDATE email_change SET used = true\n" +
        "WHERE email_change_id = :email_change_id";
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withBigint("email_change_id", id).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
