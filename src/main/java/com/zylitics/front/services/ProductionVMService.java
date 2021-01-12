package com.zylitics.front.services;

import com.zylitics.front.api.VMService;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.BuildVM;
import com.zylitics.front.model.NewBuildVM;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

public class ProductionVMService implements VMService {
  
  private static final int RESPONSE_TIMEOUT_MIN = 10;
  
  private final WebClient webClient;
  
  private final APICoreProperties apiCoreProperties;
  
  public ProductionVMService(WebClient.Builder webClientBuilder,
                             APICoreProperties apiCoreProperties) {
    this.apiCoreProperties = apiCoreProperties;
    APICoreProperties.Services servicesProps = apiCoreProperties.getServices();
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMinutes(RESPONSE_TIMEOUT_MIN));
    this.webClient = webClientBuilder
        .baseUrl(servicesProps.getWzgpEndpoint() + "/" + servicesProps.getWzgpVersion())
        .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }
  
  // TODO: implement after the vm manager api is fixed per new structure.
  @Override
  public BuildVM newBuildVM(NewBuildVM newBuildVM) {
    throw new RuntimeException("Not yet implemented");
  }
}
