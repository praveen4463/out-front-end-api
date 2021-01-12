package com.zylitics.front.dao;

import com.zylitics.front.model.BuildStatus;
import com.zylitics.front.model.TestStatus;
import com.zylitics.front.provider.BuildStatusProvider;
import com.zylitics.front.util.DateTimeUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoBuildStatusProvider extends AbstractDaoProvider implements BuildStatusProvider {
  
  DaoBuildStatusProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<BuildStatus> getBuildStatus(int buildId, int versionId, int userId) {
    String sql = "SELECT status, zwl_executing_line,\n" +
        "start_date AT TIME ZONE 'UTC' AS start_date,\n" +
        "end_date AT TIME ZONE 'UTC' AS end_date,\n" +
        "error, error_from_pos, error_to_pos FROM bt_build_status\n" +
        "WHERE bt_build_id = :bt_build_id AND bt_test_version_id = :bt_test_version_id\n" +
        "AND zluser_id = :zluser_id";
    List<BuildStatus> bs = jdbc.query(sql, new SqlParamsBuilder(userId)
        .withInteger("bt_build_id", buildId)
        .withInteger("bt_test_version_id", versionId).build(), (rs, rowNum) ->
        new BuildStatus()
            .setStatus(TestStatus.valueOf(rs.getString("status")))
            .setZwlExecutingLine(rs.getInt("zwl_executing_line"))
            .setStartDate(DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp("start_date")))
            .setEndDate(DateTimeUtil.sqlTimestampToLocal(rs.getTimestamp("end_date")))
            .setError(rs.getString("error"))
            .setErrorFromPos(rs.getString("error_from_pos"))
            .setErrorToPos(rs.getString("error_to_pos")));
    if (bs.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(bs.get(0));
  }
}
