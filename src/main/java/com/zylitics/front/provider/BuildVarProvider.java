package com.zylitics.front.provider;

import com.zylitics.front.model.BuildVar;

import java.util.List;
import java.util.Optional;

public interface BuildVarProvider {
  
  Optional<Integer> newBuildVar(BuildVar buildVar, int projectId, int userId);
  
  List<BuildVar> getBuildVars (int projectId, int userId);
  
  int updateBuildVar(String columnId, String value, int buildVarId, int projectId, int userId);
  
  int deleteBuildVar(int buildVarId, boolean isPrimary, int userId);
}
