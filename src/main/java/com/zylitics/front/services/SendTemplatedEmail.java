package com.zylitics.front.services;

import com.zylitics.front.model.EmailInfo;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

public class SendTemplatedEmail {
  
  private final EmailInfo emailInfo;
  
  private final String templateId;
  
  @Nullable
  private final Map<String, Object> templateData;
  
  @Nullable
  private final Integer unsubscribeGroupId;
  
  @Nullable
  private final int[] unsubscribeGroupsToShow;
  
  public SendTemplatedEmail(EmailInfo emailInfo,
                     String templateId) {
    this(emailInfo, templateId, null, null, null);
  }
  
  public SendTemplatedEmail(EmailInfo emailInfo,
                     String templateId,
                     Map<String, Object> templateData) {
    this(emailInfo, templateId, templateData, null, null);
  }
  
  public SendTemplatedEmail(EmailInfo emailInfo,
                     String templateId,
                     @Nullable Map<String, Object> templateData,
                     @Nullable Integer unsubscribeGroupId,
                     @Nullable int[] unsubscribeGroupsToShow) {
    this.emailInfo = emailInfo;
    this.templateId = templateId;
    this.templateData = templateData;
    this.unsubscribeGroupId = unsubscribeGroupId;
    this.unsubscribeGroupsToShow = unsubscribeGroupsToShow;
  }
  
  public EmailInfo getEmailInfo() {
    return emailInfo;
  }
  
  public String getTemplateId() {
    return templateId;
  }
  
  @Nullable
  public Map<String, Object> getTemplateData() {
    return templateData;
  }
  
  @Nullable
  public Integer getUnsubscribeGroupId() {
    return unsubscribeGroupId;
  }
  
  @Nullable
  public int[] getUnsubscribeGroupsToShow() {
    return unsubscribeGroupsToShow;
  }
  
  @Override
  public String toString() {
    return "SendTemplatedEmail{" +
        "emailInfo=" + emailInfo +
        ", templateId='" + templateId + '\'' +
        ", templateData=" + templateData +
        ", unsubscribeGroupId=" + unsubscribeGroupId +
        ", unsubscribeGroupsToShow=" + Arrays.toString(unsubscribeGroupsToShow) +
        '}';
  }
}
