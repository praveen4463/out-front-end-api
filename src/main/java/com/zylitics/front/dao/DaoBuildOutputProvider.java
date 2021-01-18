package com.zylitics.front.dao;

import com.google.common.base.Strings;
import com.zylitics.front.model.BuildOutput;
import com.zylitics.front.provider.BuildOutputProvider;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.Optional;

@Repository
public class DaoBuildOutputProvider extends AbstractDaoProvider implements BuildOutputProvider {
  
  DaoBuildOutputProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public Optional<BuildOutput> getOutput(int buildId, int versionId,
                                         @Nullable String nextOutputToken) {
    StringBuilder sql = new StringBuilder("SELECT max(bt_build_output_id) AS max_id,\n" +
        "string_agg(output, '\n' ORDER BY bt_build_output_id) AS outputs,\n" +
        "bool_or(ended) is_ended FROM bt_build_output\n" +
        "WHERE bt_build_id = :bt_build_id AND bt_test_version_id = :bt_test_version_id\n");
    boolean isTokenEmpty = Strings.isNullOrEmpty(nextOutputToken);
    if (!isTokenEmpty) {
      sql.append("AND ").append(nextOutputToken);
    }
    sql.append("GROUP BY bt_build_id, bt_test_version_id");
    SqlParameterSource params = new SqlParamsBuilder()
        .withInteger("bt_build_id", buildId)
        .withInteger("bt_test_version_id", versionId).build();
    BuildOutput buildOutput = jdbc.query(sql.toString(), params, (rs) -> {
      if (!rs.next()) {
        return null;
      }
      long maxId = rs.getLong("max_id");
      boolean ended = rs.getBoolean("is_ended");
      BuildOutput res = new BuildOutput().setOutputsWithLineBreak(rs.getString("outputs"));
      if (!ended) {
        res.setNextOutputToken("bt_build_output_id > " + maxId);
      }
      return res;
    });
    // when no output is found, return the token received if given.
    if (buildOutput == null && !isTokenEmpty) {
      return Optional.of(new BuildOutput().setNextOutputToken(nextOutputToken));
    }
    return Optional.ofNullable(buildOutput);
  }
}
