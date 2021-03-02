package com.zylitics.front.model;

// dates are in long as all our dates returned to front end are in epoch seconds
public class UsersPlanResponse {
  
  private PlanType planType;
  
  private String planName;
  
  private String displayName;
  
  private int consumedMinutes;
  
  private int totalParallel;
  
  private int totalMinutes;
  
  private long billingCycleStart;
  
  private long billingCyclePlannedEnd;
  
  public PlanType getPlanType() {
    return planType;
  }
  
  public UsersPlanResponse setPlanType(PlanType planType) {
    this.planType = planType;
    return this;
  }
  
  public String getPlanName() {
    return planName;
  }
  
  public UsersPlanResponse setPlanName(String planName) {
    this.planName = planName;
    return this;
  }
  
  public String getDisplayName() {
    return displayName;
  }
  
  public UsersPlanResponse setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }
  
  public int getConsumedMinutes() {
    return consumedMinutes;
  }
  
  public UsersPlanResponse setConsumedMinutes(int consumedMinutes) {
    this.consumedMinutes = consumedMinutes;
    return this;
  }
  
  public int getTotalParallel() {
    return totalParallel;
  }
  
  public UsersPlanResponse setTotalParallel(int totalParallel) {
    this.totalParallel = totalParallel;
    return this;
  }
  
  public int getTotalMinutes() {
    return totalMinutes;
  }
  
  public UsersPlanResponse setTotalMinutes(int totalMinutes) {
    this.totalMinutes = totalMinutes;
    return this;
  }
  
  public long getBillingCycleStart() {
    return billingCycleStart;
  }
  
  public UsersPlanResponse setBillingCycleStart(long billingCycleStart) {
    this.billingCycleStart = billingCycleStart;
    return this;
  }
  
  public long getBillingCyclePlannedEnd() {
    return billingCyclePlannedEnd;
  }
  
  public UsersPlanResponse setBillingCyclePlannedEnd(long billingCyclePlannedEnd) {
    this.billingCyclePlannedEnd = billingCyclePlannedEnd;
    return this;
  }
}
