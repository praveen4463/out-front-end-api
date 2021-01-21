package com.zylitics.front.provider;

import com.zylitics.front.model.BrowserNameToVersions;

import java.util.List;

public interface BrowserProvider {
  
  List<BrowserNameToVersions> getBrowsers(String platform);
}
