package com.zylitics.front.provider;

import com.zylitics.front.model.BuildOutput;
import com.zylitics.front.model.BuildOutputDetailsByVersion;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface BuildOutputProvider {
  
  Optional<BuildOutput> getOutput(int buildId,
                                  int versionId,
                                  @Nullable String nextOutputToken);
  
  List<BuildOutputDetailsByVersion> getBuildOutputDetails(int buildId, int userId);
  
  Optional<BuildOutputDetailsByVersion> getVersionOutputDetails(int buildId,
                                                                int versionId,
                                                                int userId);
}
