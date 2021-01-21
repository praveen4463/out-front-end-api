package com.zylitics.front.model;

public class ShotBasicDetails {
  
  private long totalShots;
  
  private String firstShot;
  
  private String lastShot;
  
  public long getTotalShots() {
    return totalShots;
  }
  
  public ShotBasicDetails setTotalShots(long totalShots) {
    this.totalShots = totalShots;
    return this;
  }
  
  public String getFirstShot() {
    return firstShot;
  }
  
  public ShotBasicDetails setFirstShot(String firstShot) {
    this.firstShot = firstShot;
    return this;
  }
  
  public String getLastShot() {
    return lastShot;
  }
  
  public ShotBasicDetails setLastShot(String lastShot) {
    this.lastShot = lastShot;
    return this;
  }
}
