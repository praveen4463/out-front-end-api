package com.zylitics.front.provider;

import com.zylitics.front.model.BuildOutput;

import javax.annotation.Nullable;
import java.util.Optional;

public interface BuildOutputProvider {
  
  Optional<BuildOutput> getOutput(int buildId,
                                  int versionId,
                                  @Nullable String nextOutputToken);
}
