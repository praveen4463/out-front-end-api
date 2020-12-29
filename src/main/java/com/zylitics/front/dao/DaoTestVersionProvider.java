package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.TestVersion;
import com.zylitics.front.provider.TestVersionProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

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
  public void renameVersion(TestVersion testVersion, int userId) {
    Preconditions.checkArgument(testVersion.getId() > 0, "versionId is required");
  
    String sql = "SELECT count(*) FROM bt_test_version\n" +
        "WHERE bt_test_version_id <> :bt_test_version_id\n" +
        "AND name ILIKE lower(:name) AND bt_test_id = :bt_test_id;";
    int matchingNames = jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("bt_test_version_id", testVersion.getId())
        .withVarchar("name", testVersion.getName())
        .withInteger("bt_test_id", testVersion.getTestId()).build(), CommonUtil.getSingleInt())
        .get(0);
    if (matchingNames > 0) {
      throw new IllegalArgumentException("Can't rename version, given name already exists");
    }
    sql = "UPDATE bt_test_version AS v SET name = :name\n" +
        "FROM bt_test AS t INNER JOIN bt_file AS f ON (t.bt_file_id = f.bt_file_id)\n" +
        "INNER JOIN bt_project p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE bt_test_version_id = :bt_test_version_id AND v.bt_test_id = t.bt_test_id\n" +
        "AND p.zluser_id = :zluser_id;";
    int result = jdbc.update(sql, new SqlParamsBuilder(userId)
        .withVarchar("name", testVersion.getName())
        .withInteger("bt_test_version_id", testVersion.getId()).build());
    CommonUtil.validateSingleRowDbCommit(result);
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