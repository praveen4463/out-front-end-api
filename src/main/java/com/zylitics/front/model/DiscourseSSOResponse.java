package com.zylitics.front.model;

public class DiscourseSSOResponse {
  
  private String sso;
  
  private String sig;
  
  public String getSso() {
    return sso;
  }
  
  public DiscourseSSOResponse setSso(String sso) {
    this.sso = sso;
    return this;
  }
  
  public String getSig() {
    return sig;
  }
  
  public DiscourseSSOResponse setSig(String sig) {
    this.sig = sig;
    return this;
  }
}
