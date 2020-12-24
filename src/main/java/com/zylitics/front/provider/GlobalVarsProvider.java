package com.zylitics.front.provider;

import com.zylitics.front.model.GlobalVars;

import java.util.List;

public interface GlobalVarsProvider {
  
  List<GlobalVars> getGlobalVars(int projectId, int userId);
}
