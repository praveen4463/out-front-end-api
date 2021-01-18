package com.zylitics.front.dao;

import com.zylitics.front.model.BuildVM;
import com.zylitics.front.provider.BuildVMProvider;
import com.zylitics.front.util.CommonUtil;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DaoBuildVMProvider extends AbstractDaoProvider implements BuildVMProvider {
  
  public DaoBuildVMProvider(NamedParameterJdbcTemplate jdbc) {
    super(jdbc);
  }
  
  @Override
  public int newBuildVM(BuildVM buildVM) {
    String sql = "INSERT INTO bt_build_vm (internal_ip, name, zone, delete_from_runner)\n" +
        "VALUES (:internal_ip, :name, :zone, :delete_from_runner) RETURNING bt_build_vm_id";
    return jdbc.query(sql, new SqlParamsBuilder()
        .withOther("internal_ip", buildVM.getInternalIp())
        .withOther("name", buildVM.getName())
        .withOther("zone", buildVM.getZone())
        .withBoolean("delete_from_runner", buildVM.isDeleteFromRunner())
        .build(), CommonUtil.getSingleInt()).get(0);
  }
  
  @Override
  public Optional<BuildVM> getBuildVMByBuild(int buildId, int userId) {
    String sql = "SELECT bv.bt_build_vm_id, bv.internal_ip, bv.name, bv.zone,\n" +
        "bv.delete_from_runner FROM bt_build_vm AS bv\n" +
        "INNER JOIN bt_build AS bu ON (bv.bt_build_vm_id = bu.bt_build_vm_id)\n" +
        "INNER JOIN bt_project AS p ON (bu.bt_project_id = p.bt_project_id)\n" +
        "WHERE bu.bt_build_id = :bt_build_id AND p.zluser_id = :zluser_id";
    List<BuildVM> vms = jdbc.query(sql, new SqlParamsBuilder(userId)
        .withInteger("bt_build_id", buildId).build(), (rs, rowNum) ->
        new BuildVM()
            .setId(rs.getInt("bt_build_vm_id"))
            .setInternalIp(rs.getString("internal_ip"))
            .setName(rs.getString("name"))
            .setZone(rs.getString("zone"))
            .setDeleteFromRunner(rs.getBoolean("delete_from_runner")));
    if (vms.size() == 0) {
      return Optional.empty();
    }
    return Optional.of(vms.get(0));
  }
}
