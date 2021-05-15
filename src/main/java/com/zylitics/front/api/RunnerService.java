package com.zylitics.front.api;

public interface RunnerService {
  
  String newSession(String runnerIP, int buildId);
  
  boolean stopBuild(String runnerIP, int buildId);
}
