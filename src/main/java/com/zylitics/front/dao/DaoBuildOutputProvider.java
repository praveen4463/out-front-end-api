package com.zylitics.front.dao;

import com.google.common.base.Strings;
import com.zylitics.front.model.BuildOutput;
import com.zylitics.front.model.BuildOutputDetailsByVersion;
import com.zylitics.front.provider.BuildOutputProvider;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.util.List;
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
      // validate token
      if (!nextOutputToken.matches("bt_build_output_id > \\d+")) {
        throw new IllegalArgumentException("Corrupted token was supplied");
      }
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
  
  private List<BuildOutputDetailsByVersion> getBuildOutputDetailsByVersionList(
      int buildId,
      int userId,
      @Nullable Integer versionId) {
    // a right join is used so that even if we push an error for some version without any output, we
    // still get that error and null output.
    String sql = "SELECT bt_test_version_id,\n" +
        "string_agg(output, '\n' ORDER BY bt_build_output_id) AS outputs, min(error) error\n" +
        "FROM bt_build_output RIGHT JOIN bt_build_status\n" +
        "USING (bt_build_id, bt_test_version_id)\n" +
        "WHERE bt_build_id = :bt_build_id AND zluser_id = :zluser_id\n";
    SqlParamsBuilder paramsBuilder = new SqlParamsBuilder(userId)
        .withInteger("bt_build_id", buildId);
    if (versionId != null) {
      sql += "AND bt_test_version_id = :bt_test_version_id\n";
      paramsBuilder.withInteger("bt_test_version_id", versionId);
    }
    sql += "GROUP BY bt_build_id, bt_test_version_id";
    return jdbc.query(sql, paramsBuilder.build(), (rs, rowNum) ->
        new BuildOutputDetailsByVersion()
            .setBuildId(buildId)
            .setVersionId(rs.getInt("bt_test_version_id"))
            .setOutputsWithLineBreak(rs.getString("outputs"))
            .setError(rs.getString("error")));
  }
  
  @Override
  public List<BuildOutputDetailsByVersion> getBuildOutputDetails(int buildId, int userId) {
    return getBuildOutputDetailsByVersionList(buildId, userId, null);
  }
  
  @Override
  public Optional<BuildOutputDetailsByVersion> getVersionOutputDetails(int buildId,
                                                                       int versionId,
                                                                       int userId) {
    List<BuildOutputDetailsByVersion> outputs =
        getBuildOutputDetailsByVersionList(buildId, userId, versionId);
    if (outputs.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(outputs.get(0));
  }
}
