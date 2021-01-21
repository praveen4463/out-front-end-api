package com.zylitics.front.provider;

import com.zylitics.front.model.ShotBasicDetails;

import javax.annotation.Nullable;
import java.util.Optional;

public interface ShotProvider {
  
  Optional<String> getLatestShot(int buildId);
  
  Optional<ShotBasicDetails> getShotBasicDetails(int buildId, @Nullable Integer versionId);
}
