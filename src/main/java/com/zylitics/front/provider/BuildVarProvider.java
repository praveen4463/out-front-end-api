package com.zylitics.front.provider;

import com.zylitics.front.model.BuildVar;

import java.util.List;

public interface BuildVarProvider {
  
  int newBuildVar(BuildVar buildVar, int projectId, int userId);
  
  List<BuildVar> getBuildVars (int projectId, int userId);
  
  void updateBuildVar(String columnId, String value, int buildVarId, int projectId, int userId);
  
  void deleteBuildVar(int buildVarId, boolean isPrimary, int projectId, int userId);
}
