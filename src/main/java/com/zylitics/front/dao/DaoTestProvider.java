package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.zylitics.front.model.Test;
import com.zylitics.front.model.TestVersion;
import com.zylitics.front.provider.TestProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;

@Repository
public class DaoTestProvider extends AbstractDaoProvider implements TestProvider {
  
  private static final String FIRST_DEFAULT_VERSION_NAME = "v1";
  
  private static final String FIRST_DEFAULT_VERSION_CODE = ""; // don't set a null to code
  
  private final TransactionTemplate transactionTemplate;
  
  @Autowired
  DaoTestProvider(NamedParameterJdbcTemplate jdbc, TransactionTemplate transactionTemplate) {
    super(jdbc);
    this.transactionTemplate = transactionTemplate;
  }
  
  @Override
  public Test newTest(Test test, int userId) {
    String sql = "SELECT count(*) FROM bt_test WHERE name ILIKE lower(:name)\n" +
        "AND bt_file_id = :bt_file_id";
    int matchingTest = jdbc.query(sql, new SqlParamsBuilder()
        .withVarchar("name", test.getName())
        .withInteger("bt_file_id", test.getFileId()).build(), CommonUtil.getSingleInt()).get(0);
    if (matchingTest > 0) {
      throw new IllegalArgumentException("Can't add test, given name already exists");
    }
    
    return transactionTemplate.execute((ts) -> {
      String sqlTest = "INSERT INTO bt_test (name, bt_file_id, create_date)\n" +
          "SELECT :name, bt_file_id, :create_date FROM bt_file AS f\n" +
          "INNER JOIN bt_project AS p ON (f.bt_project_id = p.bt_project_id)\n" +
          "WHERE f.bt_file_id = :bt_file_id AND p.zluser_id = :zluser_id\n" +
          "RETURNING bt_test_id;";
      int testId  = jdbc.query(sqlTest, new SqlParamsBuilder(userId)
          .withVarchar("name", test.getName())
          .withInteger("bt_file_id", test.getFileId())
          .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
      // no need to join bt_project again as we've verified user's project and file above
      String sqlVersion = "INSERT INTO bt_test_version (name, bt_test_id, code, create_date)\n" +
          "VALUES (:name, :bt_test_id, :code, :create_date) RETURNING bt_test_version_id;";
      int versionId = jdbc.query(sqlVersion, new SqlParamsBuilder()
          .withVarchar("name", FIRST_DEFAULT_VERSION_NAME)
          .withInteger("bt_test_id", testId)
          .withOther("code", FIRST_DEFAULT_VERSION_CODE)
          .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
      return new Test()
          .setId(testId)
          .setName(test.getName())
          .setFileId(test.getFileId())
          .setVersions(Collections.singletonList(new TestVersion()
              .setId(versionId)
              .setName(FIRST_DEFAULT_VERSION_NAME)
              .setTestId(testId)
              .setCode(FIRST_DEFAULT_VERSION_CODE)
              .setIsCurrent(true)));
    });
  }
  
  @Override
  public void renameTest(Test test, int userId) {
    Preconditions.checkArgument(test.getId() > 0, "testId is required");
    
    String sql = "SELECT count(*) FROM bt_test WHERE bt_test_id <> :bt_test_id\n" +
        "AND name ILIKE lower(:name) AND bt_file_id = :bt_file_id;";
    int matchingNames = jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("bt_test_id", test.getId())
        .withVarchar("name", test.getName())
        .withInteger("bt_file_id", test.getFileId()).build(), CommonUtil.getSingleInt()).get(0);
    if (matchingNames > 0) {
      throw new IllegalArgumentException("Can't rename test, given name already exists");
    }
    sql = "UPDATE bt_test AS t SET name = :name\n" +
        "FROM bt_file AS f INNER JOIN bt_project p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE bt_test_id = :bt_test_id AND t.bt_file_id = f.bt_file_id\n" +
        "AND p.zluser_id = :zluser_id;";
    int result = jdbc.update(sql, new SqlParamsBuilder(userId)
        .withVarchar("name", test.getName())
        .withInteger("bt_test_id", test.getId()).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public void deleteTest(int testId, int userId) {
    // This will delete all versions via cascade
    String sql = "DELETE FROM bt_test AS t\n" +
        "USING bt_file AS f INNER JOIN bt_project AS p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE bt_test_id = :bt_test_id AND t.bt_file_id = f.bt_file_id\n" +
        "AND p.zluser_id = :zluser_id;";
    int result = jdbc.update(sql, new SqlParamsBuilder(userId)
        .withInteger("bt_test_id", testId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
