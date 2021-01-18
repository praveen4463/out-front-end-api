package com.zylitics.front.services;

import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.util.UrlChecker;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RunnerService {
  
  private static final int RESPONSE_TIMEOUT_MIN = 2;
  
  private static final int VM_AVAILABILITY_TIMEOUT_MIN = 2;
  
  private final WebClient webClient;
  
  private final APICoreProperties apiCoreProperties;
  
  public RunnerService(WebClient.Builder webClientBuilder,
                       APICoreProperties apiCoreProperties) {
    this.apiCoreProperties = apiCoreProperties;
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMinutes(RESPONSE_TIMEOUT_MIN));
    this.webClient = webClientBuilder
        .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }
  
  public String newSession(String runnerIP, int buildId) {
    APICoreProperties.Services servicesProps = apiCoreProperties.getServices();
    String baseUrl = buildBaseUrl(runnerIP, servicesProps);
    String statusEndpoint = "/status";
    // if any timeout occurs while polling for status, let exception throw
    new UrlChecker().waitUntilAvailable(VM_AVAILABILITY_TIMEOUT_MIN, TimeUnit.MINUTES,
        baseUrl + statusEndpoint);
    String buildsEndpoint = "/builds";
    // let exception throw when api returns error, we don't need to send that error to user.
    NewSessionResponse response = webClient.post()
        .uri(baseUrl + buildsEndpoint)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new NewSessionRequest().setBuildId(buildId))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(NewSessionResponse.class).block();
    if (response == null) {
      throw new RuntimeException("Unexpectedly got empty response");
    }
    return response.getSessionId();
  }
  
  public boolean stopBuild(String runnerIP, int buildId) {
    APICoreProperties.Services servicesProps = apiCoreProperties.getServices();
    String baseUrl = buildBaseUrl(runnerIP, servicesProps);
    String stopBuildEndpoint = "/builds/{buildId}";
    // let exception throw when api returns error, we don't need to send that error to user.
    ResponseEntity<Void> response = webClient.delete()
        .uri(baseUrl + stopBuildEndpoint, buildId)
        .retrieve()
        .toBodilessEntity().block();
    if (response == null) {
      throw new RuntimeException("Unexpectedly got empty response");
    }
    return response.getStatusCode() == HttpStatus.OK;
  }
  
  private String buildBaseUrl(String runnerIP, APICoreProperties.Services servicesProps) {
    return String.format("http://%s:%s/%s", runnerIP, servicesProps.getBtbrPort(),
        servicesProps.getBtbrVersion());
  }
  
  private static class NewSessionRequest {
    
    private int buildId;
  
    public int getBuildId() {
      return buildId;
    }
  
    public NewSessionRequest setBuildId(int buildId) {
      this.buildId = buildId;
      return this;
    }
  }
  
  private static class NewSessionResponse {
    
    private String sessionId;
  
    public String getSessionId() {
      return sessionId;
    }
  
    @SuppressWarnings("unused")
    public NewSessionResponse setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }
  }
}
