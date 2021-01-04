package com.zylitics.front.model;

public class RunError {
  
  private String msg;
  
  private LineInfo from;
  
  private LineInfo to;
  
  public static class LineInfo {
    
    private int line;
    
    private int ch;
  
    @SuppressWarnings("unused")
    public int getLine() {
      return line;
    }
  
    public LineInfo setLine(int line) {
      this.line = line;
      return this;
    }
  
    @SuppressWarnings("unused")
    public int getCh() {
      return ch;
    }
  
    public LineInfo setCh(int ch) {
      this.ch = ch;
      return this;
    }
  }
  
  @SuppressWarnings("unused")
  public String getMsg() {
    return msg;
  }
  
  public RunError setMsg(String msg) {
    this.msg = msg;
    return this;
  }
  
  public LineInfo getFrom() {
    return from;
  }
  
  public RunError setFrom(LineInfo from) {
    this.from = from;
    return this;
  }
  
  public LineInfo getTo() {
    return to;
  }
  
  public RunError setTo(LineInfo to) {
    this.to = to;
    return this;
  }
}
