package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.TestVersion;
import com.zylitics.front.model.TestVersionRename;
import com.zylitics.front.provider.TestVersionProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.JDBCType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class DaoTestVersionProvider extends AbstractDaoProvider implements TestVersionProvider {
  
  private final TransactionTemplate transactionTemplate;
  
  @Autowired
  DaoTestVersionProvider(NamedParameterJdbcTemplate jdbc, TransactionTemplate transactionTemplate) {
    super(jdbc);
    this.transactionTemplate = transactionTemplate;
  }
  
  @Override
  public TestVersion newVersion(TestVersion testVersion, int userId) {
    String sql = "SELECT count(*) FROM bt_test_version WHERE name ILIKE lower(:name)\n" +
        "AND bt_test_id = :bt_test_id";
    int matchingVersions = jdbc.query(sql, new SqlParamsBuilder()
        .withVarchar("name", testVersion.getName())
        .withInteger("bt_test_id", testVersion.getTestId()).build(), CommonUtil.getSingleInt())
        .get(0);
    if (matchingVersions > 0) {
      throw new IllegalArgumentException("Can't add version, given name already exists");
    }
    
    return transactionTemplate.execute(ts -> {
      // every time a new version is added, there is already an existing version that is current
      // version for the test because no test can remain without a version. Thus the following
      // update must update 1 row.
      String updateSql = "UPDATE bt_test_version AS v SET is_current = false \n" +
          "FROM bt_test AS t INNER JOIN bt_file AS f ON (t.bt_file_id = f.bt_file_id)\n" +
          "INNER JOIN bt_project p ON (f.bt_project_id = p.bt_project_id)\n" +
          "WHERE v.bt_test_id = t.bt_test_id AND v.bt_test_id = :bt_test_id\n" +
          "AND v.is_current = true AND p.zluser_id = :zluser_id RETURNING bt_test_version_id";
      // !! queryForObject is needed here !!
      // following will throw exception if other than 1 row is returned by the update, we are
      // returning testVersionId that was latest because it is needed to get last version's code
      Integer lastCurrentVersionId = jdbc.queryForObject(updateSql, new SqlParamsBuilder(userId)
          .withInteger("bt_test_id", testVersion.getTestId()).build(), Integer.class);
      Objects.requireNonNull(lastCurrentVersionId);
      
      String insertSql = "INSERT INTO bt_test_version (name, bt_test_id, code, create_date)\n" +
          "VALUES (:name, :bt_test_id," +
          "(SELECT code FROM bt_test_version WHERE bt_test_version_id = :bt_test_version_id)" +
          ", :create_date) RETURNING *";
      return jdbc.query(insertSql,
          new SqlParamsBuilder()
              .withVarchar("name", testVersion.getName())
              .withInteger("bt_test_id", testVersion.getTestId())
              .withInteger("bt_test_version_id", lastCurrentVersionId)
              .withCreateDate().build(),
          (rs, rowNum) -> new TestVersion()
              .setId(rs.getInt("bt_test_version_id"))
              .setName(testVersion.getName())
              .setTestId(testVersion.getTestId())
              .setCode(rs.getString("code"))
              .setIsCurrent(rs.getBoolean("is_current"))).get(0);
    });
  }
  
  @Override
  public void renameVersion(int versionId, TestVersionRename testVersionRename, int userId) {
    String sql = "SELECT count(*) FROM bt_test_version\n" +
        "WHERE bt_test_version_id <> :bt_test_version_id\n" +
        "AND name ILIKE lower(:name) AND bt_test_id = :bt_test_id;";
    int matchingNames = jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("bt_test_version_id", versionId)
        .withVarchar("name", testVersionRename.getName())
        .withInteger("bt_test_id", testVersionRename.getTestId())
        .build(), CommonUtil.getSingleInt()).get(0);
    if (matchingNames > 0) {
      throw new IllegalArgumentException("Can't rename version, given name already exists");
    }
    sql = "UPDATE bt_test_version AS v SET name = :name\n" +
        "FROM bt_test AS t INNER JOIN bt_file AS f ON (t.bt_file_id = f.bt_file_id)\n" +
        "INNER JOIN bt_project p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE bt_test_version_id = :bt_test_version_id AND v.bt_test_id = t.bt_test_id\n" +
        "AND p.zluser_id = :zluser_id";
    int result = jdbc.update(sql, new SqlParamsBuilder(userId)
        .withVarchar("name", testVersionRename.getName())
        .withInteger("bt_test_version_id", versionId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public void updateCode(int versionId, String code, int userId) {
    String sql = "UPDATE bt_test_version AS v SET code = :code\n" +
        "FROM bt_test AS t INNER JOIN bt_file AS f ON (t.bt_file_id = f.bt_file_id)\n" +
        "INNER JOIN bt_project p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE bt_test_version_id = :bt_test_version_id AND v.bt_test_id = t.bt_test_id\n" +
        "AND p.zluser_id = :zluser_id";
    int result = jdbc.update(sql, new SqlParamsBuilder(userId)
        .withOther("code", code)
        .withInteger("bt_test_version_id", versionId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public String getCode(int versionId, int userId) {
    String sql = "SELECT code FROM bt_test_version v\n" +
        "INNER JOIN bt_test AS t ON (v.bt_test_id = t.bt_test_id)\n" +
        "INNER JOIN bt_file AS f ON (t.bt_file_id = f.bt_file_id)\n" +
        "INNER JOIN bt_project p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE bt_test_version_id = :bt_test_version_id AND p.zluser_id = :zluser_id";
    List<String> existingCodes = jdbc.query(sql, new SqlParamsBuilder(userId)
        .withInteger("bt_test_version_id", versionId).build(), CommonUtil.getSingleString());
    if (existingCodes.size() == 0) {
      throw new RuntimeException("Given versionId " + versionId + " wasn't found");
    }
    return existingCodes.get(0);
  }
  
  @Override
  public Map<Integer, String> getCodes(List<Integer> versionIds, int userId) {
    Preconditions.checkArgument(versionIds.size() > 0, "versionIds can't be empty");
    Map<Integer, String> codes = new HashMap<>();
    String sql = "SELECT bt_test_version_id, code FROM bt_test_version AS v\n" +
        "INNER JOIN bt_test AS t ON (v.bt_test_id = t.bt_test_id)\n" +
        "INNER JOIN bt_file AS f ON (t.bt_file_id = f.bt_file_id)\n" +
        "INNER JOIN bt_project p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE bt_test_version_id IN (SELECT * FROM unnest(:bt_test_version_ids))\n" +
        "AND p.zluser_id = :zluser_id";
    jdbc.query(sql, new SqlParamsBuilder(userId)
        .withArray("bt_test_version_ids", versionIds.toArray(), JDBCType.INTEGER).build(),
        (rs -> {
          codes.put(rs.getInt("bt_test_version_id"), rs.getString("code"));
        }));
    if (codes.size() != versionIds.size()) {
      throw new RuntimeException("Some of given versionIds " + versionIds + " weren't found");
    }
    return codes;
  }
  
  @Override
  public boolean anyVersionHasBlankCode(List<Integer> versionIds, int userId) {
    Preconditions.checkArgument(versionIds.size() > 0, "versionIds can't be empty");
    String sql = "SELECT count(*) FROM bt_test_version AS v\n" +
        "INNER JOIN bt_test AS t ON (v.bt_test_id = t.bt_test_id)\n" +
        "INNER JOIN bt_file AS f ON (t.bt_file_id = f.bt_file_id)\n" +
        "INNER JOIN bt_project p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE bt_test_version_id IN (SELECT * FROM unnest(:bt_test_version_ids))\n" +
        "AND p.zluser_id = :zluser_id AND v.code IS NOT NULL\n" +
        "AND length(regexp_replace(v.code, '[\\n\\r\\t\\s]', '', 'g')) > 0";
    int total = jdbc.query(sql, new SqlParamsBuilder(userId)
            .withArray("bt_test_version_ids", versionIds.toArray(), JDBCType.INTEGER).build(),
        CommonUtil.getSingleInt()).get(0);
    return total < versionIds.size(); // if total is lesser than all given version it may mean two
    // things, either some version has blank code or user doesn't own some/all given versions.
  }
  
  @Override
  public void captureVersions(List<Integer> versionIds, int buildId) {
    // https://stackoverflow.com/a/35456954/1624454
    String sql =
        "INSERT INTO bt_build_tests (bt_build_id, bt_file_id, bt_file_name, bt_test_id,\n" +
        "bt_test_name, bt_test_version_id, bt_test_version_name, bt_test_version_code)\n" +
        "SELECT :bt_build_id, f.bt_file_id, f.name, t.bt_test_id, t.name, v.bt_test_version_id,\n" +
        "v.name, v.code FROM bt_file AS f\n" +
        "INNER JOIN bt_test AS t ON (f.bt_file_id = t.bt_file_id)\n" +
        "INNER JOIN bt_test_version AS v ON (t.bt_test_id = v.bt_test_id)\n" +
        "INNER JOIN unnest(:bt_test_version_ids)\n" +
        "WITH ORDINALITY temp (bt_test_version_id, ord)\n" +
        "USING (bt_test_version_id) ORDER by temp.ord"; // put versions in bt_build_tests in the same order
    // as in the versionIds list
    int result = jdbc.update(sql, new SqlParamsBuilder()
        .withInteger("bt_build_id", buildId)
        .withArray("bt_test_version_ids", versionIds.toArray(), JDBCType.INTEGER)
        .build());
    if (result != versionIds.size()) {
      throw new RuntimeException("Couldn't insert all the given versions");
    }
  }
  
  @Override
  public void deleteVersion(int versionId, int userId) {
    // version that's current one can't be deleted
    String sql = "DELETE FROM bt_test_version AS v\n" +
        "USING bt_test AS t INNER JOIN bt_file AS f ON (t.bt_file_id = f.bt_file_id)\n" +
        "INNER JOIN bt_project p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE bt_test_version_id = :bt_test_version_id AND v.is_current = false\n" +
        "AND v.bt_test_id = t.bt_test_id AND p.zluser_id = :zluser_id;";
    int result = jdbc.update(sql, new SqlParamsBuilder(userId)
        .withInteger("bt_test_version_id", versionId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}