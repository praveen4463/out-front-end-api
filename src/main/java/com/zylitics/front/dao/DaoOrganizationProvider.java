package com.zylitics.front.dao;

import com.zylitics.front.model.Organization;
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
    String sql = "SELECT name FROM organization WHERE organization_id = :organization_id";
    List<Organization> organizations = jdbc.query(sql, new SqlParamsBuilder()
        .withInteger("organization_id", id).build(), (rs, rowNum) ->
        new Organization().setName(rs.getString("name")));
    if (organizations.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(organizations.get(0));
  }
}
