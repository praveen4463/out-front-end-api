package com.zylitics.front.provider;

import com.zylitics.front.model.GlobalVar;

import java.util.List;

public interface GlobalVarProvider {
  
  int newGlobalVar(GlobalVar globalVar, int projectId, int userId);
  
  List<GlobalVar> getGlobalVars(int projectId, int userId);
  
  void updateValue(String value, int globalVarId, int userId);
  
  void deleteGlobalVar(int globalVarId, int userId);
}
