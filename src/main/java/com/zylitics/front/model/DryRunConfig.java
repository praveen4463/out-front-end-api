package com.zylitics.front.model;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Validated
public class DryRunConfig {

  private Browser browser;
  
  @NotBlank
  private String platform;
  
  @NotNull
  private Map<String, Integer> selectedBuildVarIdPerKey;
  
  public Browser getBrowser() {
    return browser;
  }
  
  @SuppressWarnings("unused")
  public DryRunConfig setBrowser(Browser browser) {
    this.browser = browser;
    return this;
  }
  
  public String getPlatform() {
    return platform;
  }
  
  @SuppressWarnings("unused")
  public DryRunConfig setPlatform(String platform) {
    this.platform = platform;
    return this;
  }
  
  public Map<String, Integer> getSelectedBuildVarIdPerKey() {
    return selectedBuildVarIdPerKey;
  }
  
  @SuppressWarnings("unused")
  public DryRunConfig setSelectedBuildVarIdPerKey(Map<String, Integer> selectedBuildVarIdPerKey) {
    this.selectedBuildVarIdPerKey = selectedBuildVarIdPerKey;
    return this;
  }
  
  @Validated
  public static class Browser {
    
    @NotBlank
    private String name;
  
    @NotBlank
    private String version;
  
    public String getName() {
      return name;
    }
  
    public Browser setName(String name) {
      this.name = name;
      return this;
    }
  
    public String getVersion() {
      return version;
    }
  
    public Browser setVersion(String version) {
      this.version = version;
      return this;
    }
  }
}
