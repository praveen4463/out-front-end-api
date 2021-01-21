package com.zylitics.front.provider;

import com.zylitics.front.model.CapturedVariable;
import com.zylitics.front.model.GlobalVar;

import java.util.List;
import java.util.Map;

public interface GlobalVarProvider {
  
  int newGlobalVar(GlobalVar globalVar, int projectId, int userId);
  
  List<GlobalVar> getGlobalVars(int projectId, int userId);
  
  void updateValue(String value, int globalVarId, int userId);
  
  void captureGlobalVars(int projectId, int buildId);
  
  void deleteGlobalVar(int globalVarId, int userId);
  
  List<CapturedVariable> getCapturedGlobalVars(int buildId, int userId);
}
