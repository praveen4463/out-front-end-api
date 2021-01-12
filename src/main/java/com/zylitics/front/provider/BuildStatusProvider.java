package com.zylitics.front.provider;

import com.zylitics.front.model.BuildStatus;

import java.util.Optional;

public interface BuildStatusProvider {
  
  Optional<BuildStatus> getBuildStatus(int buildId, int versionId, int userId);
}
