package com.zylitics.front.provider;

import com.zylitics.front.model.ZwlProgramOutput;

import javax.annotation.Nullable;
import java.util.Optional;

public interface ZwlProgramOutputProvider {
  
  Optional<ZwlProgramOutput> getOutput(int buildId,
                                       int versionId,
                                       @Nullable String nextOutputToken);
}
