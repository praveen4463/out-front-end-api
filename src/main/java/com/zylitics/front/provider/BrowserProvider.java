package com.zylitics.front.provider;

import com.zylitics.front.model.BrowserNameToVersions;

import javax.annotation.Nullable;
import java.util.List;

public interface BrowserProvider {
  
  List<BrowserNameToVersions> getBrowsers(@Nullable String platform);
}
