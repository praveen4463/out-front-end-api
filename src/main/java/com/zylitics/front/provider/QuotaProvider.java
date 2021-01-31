package com.zylitics.front.provider;

import com.zylitics.front.model.PlanName;

public interface QuotaProvider {
  
  void newQuota(PlanName planName, int organizationId);
}
