package com.zylitics.front.services;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.zylitics.front.SecretsManager;
import com.zylitics.front.api.VMService;
import com.zylitics.front.config.APICoreProperties;
import com.zylitics.front.model.BuildSourceType;
import com.zylitics.front.model.BuildVM;
import com.zylitics.front.model.NewBuildVM;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.*;

public class ProductionVMService implements VMService {
  
  private static final int RESPONSE_TIMEOUT_MIN = 10;
  
  private final Random random = new Random(); // keep it just one for entire application
  
  private final WebClient webClient;
  
  private final APICoreProperties apiCoreProperties;
  
  // TODO: !!currently the private endpoint of wzgp we're using is not authorizing requests and sending
  //  a auth header is useless. Do something in wzgp later so that all endpoints are secured even if
  //  private.
  public ProductionVMService(WebClient.Builder webClientBuilder,
                             APICoreProperties apiCoreProperties,
                             SecretsManager secretsManager) {
    this.apiCoreProperties = apiCoreProperties;
    APICoreProperties.Services services = apiCoreProperties.getServices();
    String secret = secretsManager.getSecretAsPlainText(services.getWzgpAuthSecretCloudFile());
    HttpClient httpClient = HttpClient.create()
        .responseTimeout(Duration.ofMinutes(RESPONSE_TIMEOUT_MIN));
    this.webClient = webClientBuilder
        .baseUrl(services.getWzgpEndpoint() + "/" + services.getWzgpVersion())
        .defaultHeaders(httpHeaders ->
            httpHeaders.setBasicAuth(HttpHeaders.encodeBasicAuth(services.getWzgpAuthUser(),
                secret, Charsets.UTF_8)))
        .clientConnector(new ReactorClientHttpConnector(httpClient)).build();
  }
  
  @Override
  public BuildVM newBuildVM(NewBuildVM newBuildVM) {
    APICoreProperties.Services services = apiCoreProperties.getServices();
    GetVMRequest.BuildProperties buildProperties = new GetVMRequest.BuildProperties();
    buildProperties.setBuildId(newBuildVM.getBuildId());
  
    GetVMRequest.ResourceSearchParams resourceSearchParams =
        new GetVMRequest.ResourceSearchParams();
    resourceSearchParams
        .setOs(newBuildVM.getOs())
        .setBrowser(newBuildVM.getBrowserName())
        .setShots(true);
  
    GetVMRequest.GridProperties gridProperties = new GetVMRequest.GridProperties();
    gridProperties
        .setMachineType(services.getVmMachineType())
        .setCreateExternalIP(true)
        .setMetadata(ImmutableMap.of(
            "user-screen", newBuildVM.getDisplayResolution(),
            "user-desired-browser", newBuildVM.getBrowserName() + ";" +
                newBuildVM.getBrowserVersion(),
            "time-zone-with-dst", newBuildVM.getTimezone()
    ));
  
    GetVMRequest getVMRequest = new GetVMRequest();
    getVMRequest
        .setBuildProperties(buildProperties)
        .setResourceSearchParams(resourceSearchParams)
        .setGridProperties(gridProperties);
  
    List<String> availableZones = new ArrayList<>(services.getVmZones());
    int totalZones = availableZones.size();
    String randomZone = availableZones.get(random.nextInt(totalZones));
    String endpoint = String.format("/zones/%s/grids", randomZone);
    GetVMResponse response = webClient.post()
        .uri(uriBuilder -> uriBuilder
            .path(endpoint)
            .queryParam("requireRunningVM", newBuildVM.isRequireRunningVM())
            .build())
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(getVMRequest)
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(GetVMResponse.class).block();
    if (response == null) {
      throw new RuntimeException("Unexpectedly got empty response");
    }
    return new BuildVM()
        .setInternalIp(response.getGridInternalIP())
        .setName(response.getGridName())
        .setZone(response.getZone());
  }
  
  private static class GetVMRequest {
  
    private BuildProperties buildProperties;
  
    private ResourceSearchParams resourceSearchParams;
  
    private GridProperties gridProperties;
  
    public BuildProperties getBuildProperties() {
      return buildProperties;
    }
  
    public GetVMRequest setBuildProperties(BuildProperties buildProperties) {
      this.buildProperties = buildProperties;
      return this;
    }
  
    public ResourceSearchParams getResourceSearchParams() {
      return resourceSearchParams;
    }
  
    public GetVMRequest setResourceSearchParams(ResourceSearchParams resourceSearchParams) {
      this.resourceSearchParams = resourceSearchParams;
      return this;
    }
  
    public GridProperties getGridProperties() {
      return gridProperties;
    }
  
    public GetVMRequest setGridProperties(GridProperties gridProperties) {
      this.gridProperties = gridProperties;
      return this;
    }
  
    private static class BuildProperties {
      
      private int buildId;
  
      public int getBuildId() {
        return buildId;
      }
  
      public BuildProperties setBuildId(int buildId) {
        this.buildId = buildId;
        return this;
      }
    }
  
    private static class ResourceSearchParams {
  
      private String os;
      private String browser;
      private boolean shots;
  
      public String getOs() {
        return os;
      }
  
      public ResourceSearchParams setOs(String os) {
        this.os = os;
        return this;
      }
  
      public String getBrowser() {
        return browser;
      }
  
      public ResourceSearchParams setBrowser(String browser) {
        this.browser = browser;
        return this;
      }
  
      public boolean isShots() {
        return shots;
      }
  
      public ResourceSearchParams setShots(boolean shots) {
        this.shots = shots;
        return this;
      }
    }
  
    private static class GridProperties {
  
      private String machineType;
  
      private boolean createExternalIP;
  
      private Map<String, String> metadata;
  
      public String getMachineType() {
        return machineType;
      }
  
      public GridProperties setMachineType(String machineType) {
        this.machineType = machineType;
        return this;
      }
  
      public boolean isCreateExternalIP() {
        return createExternalIP;
      }
  
      public GridProperties setCreateExternalIP(boolean createExternalIP) {
        this.createExternalIP = createExternalIP;
        return this;
      }
  
      public Map<String, String> getMetadata() {
        return metadata;
      }
  
      public GridProperties setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
      }
    }
  }
  
  private static class GetVMResponse {
    
    private String gridInternalIP;
    
    private String gridName;
    
    private String zone;
  
    public String getGridInternalIP() {
      return gridInternalIP;
    }
  
    public GetVMResponse setGridInternalIP(String gridInternalIP) {
      this.gridInternalIP = gridInternalIP;
      return this;
    }
  
    public String getGridName() {
      return gridName;
    }
  
    public GetVMResponse setGridName(String gridName) {
      this.gridName = gridName;
      return this;
    }
  
    public String getZone() {
      return zone;
    }
  
    public GetVMResponse setZone(String zone) {
      this.zone = zone;
      return this;
    }
  }
}
