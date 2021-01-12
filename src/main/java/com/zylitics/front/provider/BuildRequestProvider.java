package com.zylitics.front.provider;

import com.zylitics.front.model.BuildRequest;

import java.util.List;

public interface BuildRequestProvider {

  long newBuildRequest(BuildRequest buildRequest);
  
  List<BuildRequest> getCurrentBuildRequests(int userId);
  
  void markBuildRequestCompleted(long buildRequestId);
}
