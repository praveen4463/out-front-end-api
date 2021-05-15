package com.zylitics.front.services;

import com.zylitics.front.api.RunnerService;
import com.zylitics.front.config.APICoreProperties;
import org.springframework.web.reactive.function.client.WebClient;

public class LocalRunnerService extends ProductionRunnerService implements RunnerService {
  
  public LocalRunnerService(APICoreProperties apiCoreProperties, WebClient webClient) {
    super(apiCoreProperties, webClient);
  }
}
