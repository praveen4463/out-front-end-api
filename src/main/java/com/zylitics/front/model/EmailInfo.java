package com.zylitics.front.model;

public class EmailInfo {
  
  private String from;
  
  private String fromName;
  
  private String to;
  
  private String toName;
  
  public String getFrom() {
    return from;
  }
  
  public EmailInfo setFrom(String from) {
    this.from = from;
    return this;
  }
  
  public String getFromName() {
    return fromName;
  }
  
  public EmailInfo setFromName(String fromName) {
    this.fromName = fromName;
    return this;
  }
  
  public String getTo() {
    return to;
  }
  
  public EmailInfo setTo(String to) {
    this.to = to;
    return this;
  }
  
  public String getToName() {
    return toName;
  }
  
  public EmailInfo setToName(String toName) {
    this.toName = toName;
    return this;
  }
  
  @Override
  public String toString() {
    return "EmailInfo{" +
        "from='" + from + '\'' +
        ", fromName='" + fromName + '\'' +
        ", to='" + to + '\'' +
        ", toName='" + toName + '\'' +
        '}';
  }
}
