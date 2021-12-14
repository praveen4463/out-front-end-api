package com.zylitics.front.dao;

import com.zylitics.front.model.Organization;
import com.zylitics.front.model.Role;
import com.zylitics.front.provider.OrganizationProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoOrganizationProvider extends AbstractDaoProvider implements OrganizationProvider {
  
  DaoOrganizationProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public int newOrganization(String name) {
    String sql = "INSERT INTO organization (name, create_date)\n" +
        "VALUES (:name, :create_date) RETURNING organization_id";
    return jdbc.query(sql, new SqlParamsBuilder()
        .withOther("name", name)
        .withCreateDate().build(), CommonUtil.getSingleInt()).get(0);
  }
  
  @Override
  public Optional<Organization> getOrganization(int id) {
    String sql = "SELECT name, api_key FROM organization WHERE organization_id = :organization_id";
    List<Organization> organizations = jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("organization_id", id).build(), (rs, rowNum) ->
        new Organization()
            .setId(id)
            .setName(rs.getString("name"))
            .setApiKey(rs.getString("api_key")));
    if (organizations.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(organizations.get(0));
  }
  
  @Override
  public Organization getOrganizationOfUser(int userId) {
    String sql = "SELECT organization_id, o.name, api_key FROM zluser AS z\n" +
        "INNER JOIN organization AS o ON (z.organization_id = o.organization_id)\n" +
        "WHERE zluser_id = :zluser_id";
    List<Organization> organizations = jdbc.query(sql, new SqlParamsBuilder(userId).build(),
        (rs, rowNum) -> new Organization()
            .setId(rs.getInt("organization_id"))
            .setName(rs.getString("name"))
            .setApiKey(rs.getString("api_key")));
    return organizations.get(0);
  }
  
  @Override
  public void updateOrganizationName(int userId, String name) {
    // only admins can change org name
    String sql = "UPDATE organization AS o SET name = :name FROM zluser AS z\n" +
        "WHERE z.organization_id = o.organization_id AND zluser_id = :zluser_id AND role=:role";
    int result =
        jdbc.update(sql, new SqlParamsBuilder(userId)
            .withOther("name", name)
            .withOther("role", Role.ADMIN).build());
    CommonUtil.validateSingleRowDbCommit(result);
  }
}
