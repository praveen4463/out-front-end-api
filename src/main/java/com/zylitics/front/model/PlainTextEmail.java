package com.zylitics.front.model;

public class PlainTextEmail {
  
  private String from;
  
  private String to;
  
  private String subject;
  
  private String content;
  
  public String getFrom() {
    return from;
  }
  
  public PlainTextEmail setFrom(String from) {
    this.from = from;
    return this;
  }
  
  public String getTo() {
    return to;
  }
  
  public PlainTextEmail setTo(String to) {
    this.to = to;
    return this;
  }
  
  public String getSubject() {
    return subject;
  }
  
  public PlainTextEmail setSubject(String subject) {
    this.subject = subject;
    return this;
  }
  
  public String getContent() {
    return content;
  }
  
  public PlainTextEmail setContent(String content) {
    this.content = content;
    return this;
  }
  
  @Override
  public String toString() {
    return "PlainTextEmail{" +
        "from='" + from + '\'' +
        ", to='" + to + '\'' +
        ", subject='" + subject + '\'' +
        ", content='" + content + '\'' +
        '}';
  }
}
