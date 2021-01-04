package com.zylitics.front.runs;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class CallbackOnlyPrintStream extends PrintStream {
  
  private final Consumer<String> callbackOnPrint;
  
  public CallbackOnlyPrintStream(Consumer<String> callbackOnPrint) {
    super(new OutputStream() {
      @Override
      public void write(int b) {
        // does nothing
      }
    });
    this.callbackOnPrint = callbackOnPrint;
  }
  
  @Override
  public void println(String x) {
    callbackOnPrint.accept(x);
  }
  
  @Override
  public void println() {
    callbackOnPrint.accept("");
  }
  
  @Override
  public void println(int x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void println(char x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void println(long x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void println(float x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @SuppressWarnings("NullableProblems")
  @Override
  public void println(char[] x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void println(double x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void println(Object x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void println(boolean x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void print(String x) {
    callbackOnPrint.accept(x);
  }
  
  @Override
  public void print(int x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void print(char x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void print(long x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void print(float x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @SuppressWarnings("NullableProblems")
  @Override
  public void print(char[] x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void print(double x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void print(Object x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
  
  @Override
  public void print(boolean x) {
    callbackOnPrint.accept(String.valueOf(x));
  }
}
