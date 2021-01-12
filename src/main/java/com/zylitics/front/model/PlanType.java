package com.zylitics.front.model;

import java.util.Arrays;

public enum PlanType {
  
  FREE("Free"),
  PAID("Paid");
  
  private final String dbName;
  
  PlanType(String dbName) {
    this.dbName = dbName;
  }
  
  public static PlanType fromDbName(String dbName) {
    return Arrays.stream(PlanType.values()).filter(p -> p.getDbName().equals(dbName)).findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown plan"));
  }
  
  public String getDbName() {
    return dbName;
  }
}
