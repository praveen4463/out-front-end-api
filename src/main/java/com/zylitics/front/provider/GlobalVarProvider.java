package com.zylitics.front.provider;

import com.zylitics.front.model.GlobalVar;

import java.util.List;
import java.util.Optional;

public interface GlobalVarProvider {
  
  Optional<Integer> newGlobalVar(GlobalVar globalVar, int projectId, int userId);
  
  List<GlobalVar> getGlobalVars(int projectId, int userId);
  
  int updateValue(String value, int globalVarId, int userId);
  
  int deleteGlobalVar(int globalVarId, int userId);
}
