package com.zylitics.front.provider;

import com.zylitics.front.model.Organization;

import java.util.Optional;

public interface OrganizationProvider {
  
  int newOrganization(String name);

  Optional<Organization> getOrganization(int id);
  
  Organization getOrganizationOfUser(int userId);
  
  void updateOrganizationName(int userId, String name);
}
