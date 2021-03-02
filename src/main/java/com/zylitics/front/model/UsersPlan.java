package com.zylitics.front.model;

import java.time.LocalDateTime;

public class UsersPlan {
  
  private PlanType planType;
  
  private String planName;
  
  private String displayName;
  
  private int consumedMinutes;
  
  private int totalParallel;
  
  private int totalMinutes;
  
  private LocalDateTime billingCycleStart;
  
  private LocalDateTime billingCyclePlannedEnd;
  
  public PlanType getPlanType() {
    return planType;
  }
  
  public UsersPlan setPlanType(PlanType planType) {
    this.planType = planType;
    return this;
  }
  
  public String getPlanName() {
    return planName;
  }
  
  public UsersPlan setPlanName(String planName) {
    this.planName = planName;
    return this;
  }
  
  public String getDisplayName() {
    return displayName;
  }
  
  public UsersPlan setDisplayName(String displayName) {
    this.displayName = displayName;
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
  
  public LocalDateTime getBillingCycleStart() {
    return billingCycleStart;
  }
  
  public UsersPlan setBillingCycleStart(LocalDateTime billingCycleStart) {
    this.billingCycleStart = billingCycleStart;
    return this;
  }
  
  public LocalDateTime getBillingCyclePlannedEnd() {
    return billingCyclePlannedEnd;
  }
  
  public UsersPlan setBillingCyclePlannedEnd(LocalDateTime billingCyclePlannedEnd) {
    this.billingCyclePlannedEnd = billingCyclePlannedEnd;
    return this;
  }
}
