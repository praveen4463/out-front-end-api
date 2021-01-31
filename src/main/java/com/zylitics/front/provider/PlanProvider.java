package com.zylitics.front.provider;

import com.zylitics.front.model.Plan;
import com.zylitics.front.model.PlanName;

import java.util.Optional;

public interface PlanProvider {
  
  Optional<Plan> getPlan(PlanName planName);
}
