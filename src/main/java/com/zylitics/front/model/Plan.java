package com.zylitics.front.model;

public class Plan {
  
  private int id;
  
  private PlanName name;
  
  private String displayName;
  
  private PlanType planType;
  
  private int minutes;
  
  private int totalParallel;
  
  private long price;
  
  private int duration;
  
  private DurationUnit durationUnit;
  
  private String supportName;
  
  public int getId() {
    return id;
  }
  
  public Plan setId(int id) {
    this.id = id;
    return this;
  }
  
  public PlanName getName() {
    return name;
  }
  
  public Plan setName(PlanName name) {
    this.name = name;
    return this;
  }
  
  public String getDisplayName() {
    return displayName;
  }
  
  public Plan setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }
  
  public PlanType getPlanType() {
    return planType;
  }
  
  public Plan setPlanType(PlanType planType) {
    this.planType = planType;
    return this;
  }
  
  public int getMinutes() {
    return minutes;
  }
  
  public Plan setMinutes(int minutes) {
    this.minutes = minutes;
    return this;
  }
  
  public int getTotalParallel() {
    return totalParallel;
  }
  
  public Plan setTotalParallel(int totalParallel) {
    this.totalParallel = totalParallel;
    return this;
  }
  
  public long getPrice() {
    return price;
  }
  
  public Plan setPrice(long price) {
    this.price = price;
    return this;
  }
  
  public int getDuration() {
    return duration;
  }
  
  public Plan setDuration(int duration) {
    this.duration = duration;
    return this;
  }
  
  public DurationUnit getDurationUnit() {
    return durationUnit;
  }
  
  public Plan setDurationUnit(DurationUnit durationUnit) {
    this.durationUnit = durationUnit;
    return this;
  }
  
  public String getSupportName() {
    return supportName;
  }
  
  public Plan setSupportName(String supportName) {
    this.supportName = supportName;
    return this;
  }
}
