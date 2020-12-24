package com.zylitics.front.provider;

import com.zylitics.front.model.BuildVars;

import java.util.List;

public interface BuildVarsProvider {
  
  List<BuildVars> getBuildVars (int projectId, int userId);
}
