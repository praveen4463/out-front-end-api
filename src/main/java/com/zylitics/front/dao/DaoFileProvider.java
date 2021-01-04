package com.zylitics.front.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.zylitics.front.model.File;
import com.zylitics.front.model.FileIdentifier;
import com.zylitics.front.model.Test;
import com.zylitics.front.model.TestVersion;
import com.zylitics.front.provider.FileProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.JDBCType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Map.Entry;

@Repository
public class DaoFileProvider extends AbstractDaoProvider implements FileProvider {
  
  @Autowired
  DaoFileProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  // to verify user owns projects without separate queries, let's don't join while checking for
  // dupes but include when doing actual insertion, in case of a problem insert will fail.
  @Override
  public File newFile(File file, int projectId, int userId) {
    String sql = "SELECT count(*) FROM bt_file\n" +
        "WHERE name ILIKE lower(:name) AND bt_project_id = :bt_project_id;";
    int matchingNames = jdbc.query(sql, new SqlParamsBuilder()
        .withProject(projectId)
        .withVarchar("name", file.getName()).build(), CommonUtil.getSingleInt()).get(0);
    if (matchingNames > 0) {
      throw new IllegalArgumentException("Can't add file, given name already exists");
    }
    sql = "INSERT INTO bt_file (name, bt_project_id, create_date)\n" +
        "SELECT :name, bt_project_id, :create_date FROM bt_project\n" +
        "WHERE bt_project_id = :bt_project_id AND zluser_id = :zluser_id\n" +
        "RETURNING bt_file_id";
    return jdbc.query(sql, new SqlParamsBuilder(projectId, userId)
        .withVarchar("name", file.getName())
        .withCreateDate().build(), (rs, rowNum) ->
        new File()
            .setId(rs.getInt(1))
            .setName(file.getName())).get(0);
  }
  
  @Override
  public List<FileIdentifier> getFilesIdentifier(int projectId, int userId) {
    String sql = "SELECT f.bt_file_id, f.name FROM bt_file AS f\n" +
        "INNER JOIN bt_project AS p ON (f.bt_project_id = p.bt_project_id)\n" +
        "WHERE f.bt_project_id = :bt_project_id AND p.zluser_id = :zluser_id;";
  
    SqlParameterSource namedParams = new SqlParamsBuilder(projectId, userId).build();
    
    return jdbc.query(sql, namedParams, (rs, rowNum) -> new FileIdentifier()
        .setId(rs.getInt("bt_file_id"))
        .setName(rs.getString("name")));
  }
  
  // Don't use "IN (variable_number_of_parameter)" as JDBC have restrictions on number of parameters
  // allowed, spring says it's 100. Best is to have a table created using unnest passed with array
  // of parameters to IN clause.
  @Override
  public List<File> getFilesWithTests(List<Integer> fileIdsFilter, int userId) {
    boolean filterFiles = fileIdsFilter.size() > 0;
    // contains only files that have tests
    Map<Integer, File> fileIdToFile = new HashMap<>();
    
    Map<Integer, Entry<Integer, Test>> testIdToFileIdToTest = new HashMap<>();
  
    ListMultimap<Integer, TestVersion> testIdToVersions =
        MultimapBuilder.hashKeys().arrayListValues().build();
  
    SqlParamsBuilder fileParamsBuilder = new SqlParamsBuilder(userId);
    
    // Following query will verify that user is authorized to these files and filters files that
    // have tests.
    StringBuilder sqlFile = new StringBuilder("SELECT f.bt_file_id, f.name FROM bt_file AS f\n" +
        "INNER JOIN bt_project AS p ON (f.bt_project_id = p.bt_project_id)\n" +
        "INNER JOIN bt_test AS t ON (f.bt_file_id = t.bt_file_id)\n" +
        "WHERE p.zluser_id = :zluser_id\n");
    
    if (filterFiles) {
      sqlFile.append("AND f.bt_file_id IN (select * from unnest(:fileIds));");
      fileParamsBuilder.withArray("fileIds", fileIdsFilter.toArray(), JDBCType.INTEGER);
    } else {
      sqlFile.append("LIMIT 10;"); // limit to 10 files when no filter is given, currently no
      // nextPageToken is facilitated here. TODO: add nextPageToken here
    }
    
    jdbc.query(sqlFile.toString(), fileParamsBuilder.build(), (rs) -> {
      int fId = rs.getInt("bt_file_id");
      File f = new File()
          .setId(fId)
          .setName(rs.getString("name"));
      fileIdToFile.put(fId, f);
    });
    
    // user is either not authorized or no given files have associated tests.
    if (fileIdToFile.size() == 0) {
      return new ArrayList<>();
    }
    
    String sqlTest = "SELECT bt_test_id, name, bt_file_id\n" +
        "FROM bt_test\n" +
        "WHERE bt_file_id IN (select * from unnest(:fileIds))";
    SqlParameterSource namedParamsTest = new SqlParamsBuilder()
        .withArray("fileIds", fileIdToFile.keySet().toArray(), JDBCType.INTEGER).build();
  
    jdbc.query(sqlTest, namedParamsTest, (rs) -> {
      int fId = rs.getInt("bt_file_id");
      int tId = rs.getInt("bt_test_id");
      Test t = new Test()
          .setId(tId)
          .setName(rs.getString("name"))
          .setFileId(fId);
      testIdToFileIdToTest.put(tId, new AbstractMap.SimpleImmutableEntry<>(fId, t));
    });
  
    String sqlVersion = "SELECT bt_test_version_id, name, bt_test_id, code, is_current\n" +
        "FROM bt_test_version\n" +
        "WHERE bt_test_id IN (select * from unnest(:testIds))";
  
    SqlParameterSource namedParamsVersion = new SqlParamsBuilder()
        .withArray("testIds", testIdToFileIdToTest.keySet().toArray(), JDBCType.INTEGER).build();
    
    jdbc.query(sqlVersion, namedParamsVersion, (rs) -> {
      int tId = rs.getInt("bt_test_id");
      TestVersion v = new TestVersion()
          .setId(rs.getInt("bt_test_version_id"))
          .setName(rs.getString("name"))
          .setTestId(tId)
          .setCode(rs.getString("code"))
          .setIsCurrent(rs.getBoolean("is_current"));
      testIdToVersions.put(tId, v);
    });
    
    testIdToVersions.keySet().forEach(tId ->
        testIdToFileIdToTest.get(tId).getValue().setVersions(testIdToVersions.get(tId)));
  
    Map<Integer, List<Test>> fileIdToTests = testIdToFileIdToTest.values().stream()
        .collect(Collectors.groupingBy(Entry::getKey,
            Collectors.mapping(Entry::getValue, Collectors.toList())));
  
    fileIdToTests.keySet().forEach(fId ->
        fileIdToFile.get(fId).setTests(fileIdToTests.get(fId)));
    
    return new ArrayList<>(fileIdToFile.values());
  }
  
  @Override
  public void renameFile(File file, int projectId, int userId) {
    Preconditions.checkArgument(file.getId() > 0, "fileId is required");
    
    String sql = "SELECT count(*) FROM bt_file WHERE bt_file_id <> :bt_file_id\n" +
        "AND name ILIKE lower(:name) AND bt_project_id = :bt_project_id;";
    int matchingNames = jdbc.query(sql, new SqlParamsBuilder()
        .withProject(projectId)
        .withVarchar("name", file.getName())
        .withInteger("bt_file_id", file.getId()).build(), CommonUtil.getSingleInt()).get(0);
    if (matchingNames > 0) {
      throw new IllegalArgumentException("Can't rename file, given name already exists");
    }
    sql = "UPDATE bt_file AS f SET name = :name\n" +
        "FROM bt_project AS p WHERE bt_file_id = :bt_file_id\n" +
        "AND f.bt_project_id = p.bt_project_id AND f.bt_project_id = :bt_project_id\n" +
        "AND p.zluser_id = :zluser_id;";
    int result = jdbc.update(sql, new SqlParamsBuilder(projectId, userId)
        .withVarchar("name", file.getName())
        .withInteger("bt_file_id", file.getId()).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
  
  @Override
  public void deleteFile(int fileId, int userId) {
    // This will delete all tests and versions via cascade
    String sql = "DELETE FROM bt_file AS f\n" +
        "USING bt_project AS p WHERE bt_file_id = :bt_file_id\n" +
        "AND f.bt_project_id = p.bt_project_id AND p.zluser_id = :zluser_id;";
    int result = jdbc.update(sql, new SqlParamsBuilder(userId)
        .withInteger("bt_file_id", fileId).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
