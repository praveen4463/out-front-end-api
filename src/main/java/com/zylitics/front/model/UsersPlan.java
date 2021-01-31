package com.zylitics.front.model;

public class UsersPlan {
  
  private PlanType planType;
  
  private int consumedMinutes;
  
  private int totalParallel;
  
  private int totalMinutes;
  
  public PlanType getPlanType() {
    return planType;
  }
  
  public UsersPlan setPlanType(PlanType planType) {
    this.planType = planType;
    return this;
  }
  
  public int getConsumedMinutes() {
    return consumedMinutes;
  }
  
  public UsersPlan setConsumedMinutes(int consumedMinutes) {
    this.consumedMinutes = consumedMinutes;
    return this;
  }
  
  public int getTotalParallel() {
    return totalParallel;
  }
  
  public UsersPlan setTotalParallel(int totalParallel) {
    this.totalParallel = totalParallel;
    return this;
  }
  
  public int getTotalMinutes() {
    return totalMinutes;
  }
  
  public UsersPlan setTotalMinutes(int totalMinutes) {
    this.totalMinutes = totalMinutes;
    return this;
  }
}
