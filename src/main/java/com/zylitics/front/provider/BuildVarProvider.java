package com.zylitics.front.provider;

import com.zylitics.front.model.BuildVar;
import com.zylitics.front.model.CapturedVariable;

import java.util.List;
import java.util.Map;

public interface BuildVarProvider {
  
  int newBuildVar(BuildVar buildVar, int projectId, int userId);
  
  List<BuildVar> getBuildVars(int projectId, int userId, boolean onlyPrimary);
  
  List<BuildVar> getPrimaryBuildVarsOverridingGiven(int projectId, int userId,
                                                    Map<String, Integer> overrideKeyId);
  
  void capturePrimaryBuildVarsOverridingGiven(int projectId,
                                              Map<String, Integer> overrideKeyId,
                                              int buildId);
  
  void duplicateBuildVars(int duplicateBuildId, int originalBuildId);
  
  void updateBuildVar(String columnId, String value, int buildVarId, int projectId, int userId);
  
  void deleteBuildVar(int buildVarId, boolean isPrimary, int projectId, int userId);
  
  List<CapturedVariable> getCapturedBuildVars(int buildId, int userId);
}
