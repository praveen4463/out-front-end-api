package com.zylitics.front.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.concurrent.ThreadSafe;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

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
  
  @NotBlank
  private String kmsProjectId;
  
  public String getKmsProjectId() {
    return kmsProjectId;
  }
  
  public void setKmsProjectId(String kmsProjectId) {
    if (this.kmsProjectId == null) {
      this.kmsProjectId = kmsProjectId;
    }
  }
  
  @NotBlank
  private String frontEndBaseUrl;
  
  public String getFrontEndBaseUrl() {
    return frontEndBaseUrl;
  }
  
  public void setFrontEndBaseUrl(String frontEndBaseUrl) {
    if (this.frontEndBaseUrl == null) {
      this.frontEndBaseUrl = frontEndBaseUrl;
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
  
  @Valid
  private final Email email = new Email();
  
  public Email getEmail() { return email; }
  
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
  
    @NotBlank
    private String driverLogsDir;
  
    @NotBlank
    private String driverLogsFile;
  
    @NotBlank
    private String browserPerfLogsDir;
  
    @NotBlank
    private String browserPerfLogsFile;
    
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
  
    public String getDriverLogsDir() {
      return driverLogsDir;
    }
  
    public void setDriverLogsDir(String driverLogsDir) {
      if (this.driverLogsDir == null) {
        this.driverLogsDir = driverLogsDir;
      }
    }
  
    public String getDriverLogsFile() {
      return driverLogsFile;
    }
  
    public void setDriverLogsFile(String driverLogsFile) {
      if (this.driverLogsFile == null) {
        this.driverLogsFile = driverLogsFile;
      }
    }
  
    public String getBrowserPerfLogsDir() {
      return browserPerfLogsDir;
    }
  
    public void setBrowserPerfLogsDir(String browserPerfLogsDir) {
      if (this.browserPerfLogsDir == null) {
        this.browserPerfLogsDir = browserPerfLogsDir;
      }
    }
  
    public String getBrowserPerfLogsFile() {
      return browserPerfLogsFile;
    }
  
    public void setBrowserPerfLogsFile(String browserPerfLogsFile) {
      if (this.browserPerfLogsFile == null) {
        this.browserPerfLogsFile = browserPerfLogsFile;
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
  
    @NotBlank
    private String wzgpAuthUser;
  
    @NotBlank
    private String wzgpAuthSecretCloudFile;
    
    @NotBlank
    private String vmMachineType;
  
    @NotEmpty
    private Set<String> vmZones;
    
    @Min(1)
    private Integer btbrPort;
  
    @NotBlank
    private String btbrAuthUser;
  
    @NotBlank
    private String btbrAuthSecretCloudFile;
  
    @NotBlank
    private String localVmEnvVar;
    
    @NotBlank
    private String sendgridApiKeySecretCloudFile;
    
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
  
    public String getWzgpAuthUser() {
      return wzgpAuthUser;
    }
  
    public void setWzgpAuthUser(String wzgpAuthUser) {
      if (this.wzgpAuthUser == null) {
        this.wzgpAuthUser = wzgpAuthUser;
      }
    }
  
    public String getWzgpAuthSecretCloudFile() {
      return wzgpAuthSecretCloudFile;
    }
  
    public void setWzgpAuthSecretCloudFile(String wzgpAuthSecretCloudFile) {
      if (this.wzgpAuthSecretCloudFile == null) {
        this.wzgpAuthSecretCloudFile = wzgpAuthSecretCloudFile;
      }
    }
  
    public String getVmMachineType() {
      return vmMachineType;
    }
  
    public void setVmMachineType(String vmMachineType) {
      if (this.vmMachineType == null) {
        this.vmMachineType = vmMachineType;
      }
    }
  
    public Set<String> getVmZones() {
      return vmZones;
    }
  
    public void setVmZones(Set<String> vmZones) {
      if (this.vmZones == null) {
        this.vmZones = vmZones;
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
  
    public String getBtbrAuthUser() {
      return btbrAuthUser;
    }
  
    public void setBtbrAuthUser(String btbrAuthUser) {
      if (this.btbrAuthUser == null) {
        this.btbrAuthUser = btbrAuthUser;
      }
    }
  
    public String getBtbrAuthSecretCloudFile() {
      return btbrAuthSecretCloudFile;
    }
  
    public void setBtbrAuthSecretCloudFile(String btbrAuthSecretCloudFile) {
      if (this.btbrAuthSecretCloudFile == null) {
        this.btbrAuthSecretCloudFile = btbrAuthSecretCloudFile;
      }
    }
  
    public String getLocalVmEnvVar() {
      return localVmEnvVar;
    }
  
    public void setLocalVmEnvVar(String localVmEnvVar) {
      if (this.localVmEnvVar == null) {
        this.localVmEnvVar = localVmEnvVar;
      }
    }
  
    public String getSendgridApiKeySecretCloudFile() {
      return sendgridApiKeySecretCloudFile;
    }
  
    public void setSendgridApiKeySecretCloudFile(String sendgridApiKeySecretCloudFile) {
      if (this.sendgridApiKeySecretCloudFile == null) {
        this.sendgridApiKeySecretCloudFile = sendgridApiKeySecretCloudFile;
      }
    }
  }
  
  public static class Email {
    
    @NotBlank
    private String issueReportReceiver;
  
    @NotBlank
    private String appInternalEmailSender;
  
    @NotBlank
    private String noReplyEmailSender;
  
    @NotBlank
    private String emailBetaInviteTmpId;
  
    @NotBlank
    private String emailTeamInviteTmpId;
  
    @NotBlank
    private String emailBetaWelcomeTmpId;
  
    @NotBlank
    private String emailWelcomeTmpId;
  
    @NotBlank
    private String emailChangeTmpId;
  
    @NotBlank
    private String emailPwdResetTmpId;
  
    @NotBlank
    private String emailVerifyTmpId;
    
    @NotBlank
    private String finishSignupPage;
  
    @NotBlank
    private String pwdResetPage;
  
    @NotBlank
    private String emailChangePage;
  
    @NotBlank
    private String ctaLinkTag;
  
    @Min(1)
    private Integer marketingEmailGroupId;
  
    @Min(1)
    private Integer notificationEmailGroupId;
  
    public String getIssueReportReceiver() {
      return issueReportReceiver;
    }
  
    public void setIssueReportReceiver(String issueReportReceiver) {
      if (this.issueReportReceiver == null) {
        this.issueReportReceiver = issueReportReceiver;
      }
    }
  
    public String getAppInternalEmailSender() {
      return appInternalEmailSender;
    }
  
    public void setAppInternalEmailSender(String appInternalEmailSender) {
      if (this.appInternalEmailSender == null) {
        this.appInternalEmailSender = appInternalEmailSender;
      }
    }
  
    public String getNoReplyEmailSender() {
      return noReplyEmailSender;
    }
  
    public void setNoReplyEmailSender(String noReplyEmailSender) {
      if (this.noReplyEmailSender == null) {
        this.noReplyEmailSender = noReplyEmailSender;
      }
    }
  
    public String getEmailBetaInviteTmpId() {
      return emailBetaInviteTmpId;
    }
  
    public void setEmailBetaInviteTmpId(String emailBetaInviteTmpId) {
      if (this.emailBetaInviteTmpId == null) {
        this.emailBetaInviteTmpId = emailBetaInviteTmpId;
      }
    }
  
    public String getEmailTeamInviteTmpId() {
      return emailTeamInviteTmpId;
    }
  
    public void setEmailTeamInviteTmpId(String emailTeamInviteTmpId) {
      if (this.emailTeamInviteTmpId == null) {
        this.emailTeamInviteTmpId = emailTeamInviteTmpId;
      }
    }
  
    public String getEmailBetaWelcomeTmpId() {
      return emailBetaWelcomeTmpId;
    }
  
    public Email setEmailBetaWelcomeTmpId(String emailBetaWelcomeTmpId) {
      this.emailBetaWelcomeTmpId = emailBetaWelcomeTmpId;
      return this;
    }
  
    public String getEmailWelcomeTmpId() {
      return emailWelcomeTmpId;
    }
  
    public void setEmailWelcomeTmpId(String emailWelcomeTmpId) {
      if (this.emailWelcomeTmpId == null) {
        this.emailWelcomeTmpId = emailWelcomeTmpId;
      }
    }
  
    public String getEmailChangeTmpId() {
      return emailChangeTmpId;
    }
  
    public void setEmailChangeTmpId(String emailChangeTmpId) {
      if (this.emailChangeTmpId == null) {
        this.emailChangeTmpId = emailChangeTmpId;
      }
    }
  
    public String getEmailPwdResetTmpId() {
      return emailPwdResetTmpId;
    }
  
    public void setEmailPwdResetTmpId(String emailPwdResetTmpId) {
      if (this.emailPwdResetTmpId == null) {
        this.emailPwdResetTmpId = emailPwdResetTmpId;
      }
    }
  
    public String getEmailVerifyTmpId() {
      return emailVerifyTmpId;
    }
  
    public void setEmailVerifyTmpId(String emailVerifyTmpId) {
      if (this.emailVerifyTmpId == null) {
        this.emailVerifyTmpId = emailVerifyTmpId;
      }
    }
  
    public String getFinishSignupPage() {
      return finishSignupPage;
    }
  
    public void setFinishSignupPage(String finishSignupPage) {
      if (this.finishSignupPage == null) {
        this.finishSignupPage = finishSignupPage;
      }
    }
  
    public String getPwdResetPage() {
      return pwdResetPage;
    }
  
    public void setPwdResetPage(String pwdResetPage) {
      if (this.pwdResetPage == null) {
        this.pwdResetPage = pwdResetPage;
      }
    }
  
    public String getEmailChangePage() {
      return emailChangePage;
    }
  
    public void setEmailChangePage(String emailChangePage) {
      if (this.emailChangePage == null) {
        this.emailChangePage = emailChangePage;
      }
    }
  
    public String getCtaLinkTag() {
      return ctaLinkTag;
    }
  
    public void setCtaLinkTag(String ctaLinkTag) {
      if (this.ctaLinkTag == null) {
        this.ctaLinkTag = ctaLinkTag;
      }
    }
  
    public int getMarketingEmailGroupId() {
      return marketingEmailGroupId;
    }
  
    public void setMarketingEmailGroupId(Integer marketingEmailGroupId) {
      if (this.marketingEmailGroupId == null) {
        this.marketingEmailGroupId = marketingEmailGroupId;
      }
    }
  
    public int getNotificationEmailGroupId() {
      return notificationEmailGroupId;
    }
  
    public void setNotificationEmailGroupId(Integer notificationEmailGroupId) {
      if (this.notificationEmailGroupId == null) {
        this.notificationEmailGroupId = notificationEmailGroupId;
      }
    }
  }
}
