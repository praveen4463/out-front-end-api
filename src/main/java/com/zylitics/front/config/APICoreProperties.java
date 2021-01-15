package com.zylitics.front.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.concurrent.ThreadSafe;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * All setters in this class allow only first time access by container, after that no values can
 * be mutated.
 * @author Praveen Tiwari
 *
 */
@ThreadSafe
@Component
@ConfigurationProperties(prefix="api-core")
@Validated
@SuppressWarnings("unused")
public class APICoreProperties {
  
  @NotBlank
  private String projectId;
  
  public String getProjectId() {
    return projectId;
  }
  
  public void setProjectId(String projectId) {
    if (this.projectId == null) {
      this.projectId = projectId;
    }
  }
  
  @Valid
  private final DataSource dataSource = new DataSource();
  
  public DataSource getDataSource() { return dataSource; }
  
  @Valid
  private final CloudKms cloudKms = new CloudKms();
  
  public CloudKms getCloudKms() { return cloudKms; }
  
  @Valid
  private final Esdb esdb = new Esdb();
  
  public Esdb getEsdb() { return esdb; }
  
  @Valid
  private final Storage storage = new Storage();
  
  public Storage getStorage() {return storage;}
  
  @Valid
  private final Services services = new Services();
  
  public Services getServices() { return services; }
  
  public static class DataSource {
    
    @NotBlank
    private String dbName;
    
    @NotBlank
    private String userName;
    
    @NotBlank
    private String userSecretCloudFile;
    
    @NotBlank
    private String privateHostCloudFile;
    
    @Min(1)
    private Short minIdleConnPool;
    
    public String getDbName() {
      return dbName;
    }
    
    public void setDbName(String dbName) {
      if (this.dbName == null) {
        this.dbName = dbName;
      }
    }
    
    public String getUserName() {
      return userName;
    }
    
    public void setUserName(String userName) {
      if (this.userName == null) {
        this.userName = userName;
      }
    }
    
    public String getUserSecretCloudFile() {
      return userSecretCloudFile;
    }
    
    public void setUserSecretCloudFile(String userSecretCloudFile) {
      if (this.userSecretCloudFile == null) {
        this.userSecretCloudFile = userSecretCloudFile;
      }
    }
    
    public String getPrivateHostCloudFile() {
      return privateHostCloudFile;
    }
    
    public void setPrivateHostCloudFile(String privateHostCloudFile) {
      if (this.privateHostCloudFile == null) {
        this.privateHostCloudFile = privateHostCloudFile;
      }
    }
    
    public Short getMinIdleConnPool() {
      return minIdleConnPool;
    }
    
    public void setMinIdleConnPool(Short minIdleConnPool) {
      if (this.minIdleConnPool == null) {
        this.minIdleConnPool = minIdleConnPool;
      }
    }
  }
  
  public static class CloudKms {
    
    @NotBlank
    private String keyRing;
    
    @NotBlank
    private String key;
    
    @NotBlank
    private String keyBucket;
    
    public String getKeyRing() {
      return keyRing;
    }
    
    public void setKeyRing(String keyRing) {
      if (this.keyRing == null) {
        this.keyRing = keyRing;
      }
    }
    
    public String getKey() {
      return key;
    }
    
    public void setKey(String key) {
      if (this.key == null) {
        this.key = key;
      }
    }
    
    public String getKeyBucket() {
      return keyBucket;
    }
    
    public void setKeyBucket(String keyBucket) {
      if (this.keyBucket == null) {
        this.keyBucket = keyBucket;
      }
    }
  }
  
  public static class Esdb {
    
    @NotBlank
    private String authUser;
    
    @NotBlank
    private String authUserSecretCloudFile;
    
    @Min(1)
    private Short maxRetries;
    
    @NotBlank
    private String shotMetadataIndex;
    
    @NotBlank
    private String zwlProgramOutputIndex;
    
    @NotBlank
    private String browserIndex;
    
    @NotBlank
    private String envVarHost;
    
    public String getAuthUser() {
      return authUser;
    }
    
    public void setAuthUser(String authUser) {
      if (this.authUser == null) {
        this.authUser = authUser;
      }
    }
    
    public String getAuthUserSecretCloudFile() {
      return authUserSecretCloudFile;
    }
    
    public void setAuthUserSecretCloudFile(String authUserSecretCloudFile) {
      if (this.authUserSecretCloudFile == null) {
        this.authUserSecretCloudFile = authUserSecretCloudFile;
      }
    }
    
    public Short getMaxRetries() {
      return maxRetries;
    }
    
    public void setMaxRetries(Short maxRetries) {
      if (this.maxRetries == null) {
        this.maxRetries = maxRetries;
      }
    }
    
    public String getShotMetadataIndex() {
      return shotMetadataIndex;
    }
    
    public void setShotMetadataIndex(String shotMetadataIndex) {
      if (this.shotMetadataIndex == null) {
        this.shotMetadataIndex = shotMetadataIndex;
      }
    }
    
    public String getZwlProgramOutputIndex() {
      return zwlProgramOutputIndex;
    }
    
    public void setZwlProgramOutputIndex(String zwlProgramOutputIndex) {
      if (this.zwlProgramOutputIndex == null) {
        this.zwlProgramOutputIndex = zwlProgramOutputIndex;
      }
    }
    
    public String getBrowserIndex() {
      return browserIndex;
    }
    
    public void setBrowserIndex(String browserIndex) {
      if (this.browserIndex == null) {
        this.browserIndex = browserIndex;
      }
    }
    
    public String getEnvVarHost() {
      return envVarHost;
    }
    
    public void setEnvVarHost(String envVarHost) {
      if (this.envVarHost == null) {
        this.envVarHost = envVarHost;
      }
    }
  }
  
  public static class Storage {
  
    @NotBlank
    private String serverLogsBucket;
  
    @NotBlank
    private String elemShotsBucket;
  
    @NotBlank
    private String userDataBucket;
  
    @NotBlank
    private String userUploadsStorageDirTmpl;
  
    @NotBlank
    private String commonUploadsBucket;
    
    @Min(10)
    private Integer maxTestFileSizeMb;
  
    @Min(10)
    private Integer maxCommonFileSizeMb;
  
    public String getServerLogsBucket() {
      return serverLogsBucket;
    }
  
    public void setServerLogsBucket(String serverLogsBucket) {
      if (this.serverLogsBucket == null) {
        this.serverLogsBucket = serverLogsBucket;
      }
    }
  
    public String getElemShotsBucket() {
      return elemShotsBucket;
    }
  
    public void setElemShotsBucket(String elemShotsBucket) {
      if (this.elemShotsBucket == null) {
        this.elemShotsBucket = elemShotsBucket;
      }
    }
  
    public String getUserDataBucket() {
      return userDataBucket;
    }
  
    public void setUserDataBucket(String userDataBucket) {
      if (this.userDataBucket == null) {
        this.userDataBucket = userDataBucket;
      }
    }
  
    public String getUserUploadsStorageDirTmpl() {
      return userUploadsStorageDirTmpl;
    }
  
    public void setUserUploadsStorageDirTmpl(String userUploadsStorageDirTmpl) {
      if (this.userUploadsStorageDirTmpl == null) {
        this.userUploadsStorageDirTmpl = userUploadsStorageDirTmpl;
      }
    }
  
    public String getCommonUploadsBucket() {
      return commonUploadsBucket;
    }
  
    public void setCommonUploadsBucket(String commonUploadsBucket) {
      if (this.commonUploadsBucket == null) {
        this.commonUploadsBucket = commonUploadsBucket;
      }
    }
  
    public int getMaxTestFileSizeMb() {
      return maxTestFileSizeMb;
    }
  
    public void setMaxTestFileSizeMb(Integer maxTestFileSizeMb) {
      if (this.maxTestFileSizeMb == null) {
        this.maxTestFileSizeMb = maxTestFileSizeMb;
      }
    }
  
    public int getMaxCommonFileSizeMb() {
      return maxCommonFileSizeMb;
    }
  
    public void setMaxCommonFileSizeMb(Integer maxCommonFileSizeMb) {
      if (this.maxCommonFileSizeMb == null) {
        this.maxCommonFileSizeMb = maxCommonFileSizeMb;
      }
    }
  }
  
  public static class Services {
    
    @NotBlank
    private String wzgpEndpoint;
    
    @NotBlank
    private String wzgpVersion;
    
    @NotBlank
    private String btbrVersion;
    
    @Min(1)
    private Integer btbrPort;
  
    @NotBlank
    private String localVmEnvVar;
    
    public String getWzgpEndpoint() {
      return wzgpEndpoint;
    }
    
    public void setWzgpEndpoint(String wzgpEndpoint) {
      if (this.wzgpEndpoint == null) {
        this.wzgpEndpoint = wzgpEndpoint;
      }
    }
    
    public String getWzgpVersion() {
      return wzgpVersion;
    }
    
    public void setWzgpVersion(String wzgpVersion) {
      if (this.wzgpVersion == null) {
        this.wzgpVersion = wzgpVersion;
      }
    }
    
    public String getBtbrVersion() {
      return btbrVersion;
    }
    
    public void setBtbrVersion(String btbrVersion) {
      if (this.btbrVersion == null) {
        this.btbrVersion = btbrVersion;
      }
    }
    
    public int getBtbrPort() {
      return btbrPort;
    }
    
    public void setBtbrPort(Integer btbrPort) {
      if (this.btbrPort == null) {
        this.btbrPort = btbrPort;
      }
    }
  
    public String getLocalVmEnvVar() {
      return localVmEnvVar;
    }
  
    public Services setLocalVmEnvVar(String localVmEnvVar) {
      this.localVmEnvVar = localVmEnvVar;
      return this;
    }
  }
}
