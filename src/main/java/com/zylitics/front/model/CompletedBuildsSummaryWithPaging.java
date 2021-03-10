package com.zylitics.front.model;

import java.util.List;

public class CompletedBuildsSummaryWithPaging {
  
  private List<CompletedBuildSummary> completedBuildsSummary;
  
  private Paging paging;
  
  public List<CompletedBuildSummary> getCompletedBuildsSummary() {
    return completedBuildsSummary;
  }
  
  public CompletedBuildsSummaryWithPaging setCompletedBuildsSummary(
      List<CompletedBuildSummary> completedBuildsSummary) {
    this.completedBuildsSummary = completedBuildsSummary;
    return this;
  }
  
  public Paging getPaging() {
    return paging;
  }
  
  public CompletedBuildsSummaryWithPaging setPaging(Paging paging) {
    this.paging = paging;
    return this;
  }
}
