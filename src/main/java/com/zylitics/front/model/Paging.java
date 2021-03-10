package com.zylitics.front.model;

public class Paging {
  
  private boolean hasNewer;
  
  private boolean hasOlder;
  
  public boolean isHasNewer() {
    return hasNewer;
  }
  
  public Paging setHasNewer(boolean hasNewer) {
    this.hasNewer = hasNewer;
    return this;
  }
  
  public boolean isHasOlder() {
    return hasOlder;
  }
  
  public Paging setHasOlder(boolean hasOlder) {
    this.hasOlder = hasOlder;
    return this;
  }
}
