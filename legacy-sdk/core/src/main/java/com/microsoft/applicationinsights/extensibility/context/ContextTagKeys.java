/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.extensibility.context;

@SuppressWarnings("AbbreviationAsWordInName")
public final class ContextTagKeys {

  private static final ContextTagKeys INSTANCE = new ContextTagKeys();

  public static ContextTagKeys getKeys() {
    return INSTANCE;
  }

  public String getApplicationId() {
    return "ai.application.id";
  }

  public String getApplicationVersion() {
    return "ai.application.ver";
  }

  public String getApplicationTypeId() {
    return "ai.application.typeId";
  }

  public String getDeviceId() {
    return "ai.device.id";
  }

  public String getDeviceOS() {
    return "ai.device.os";
  }

  public String getDeviceOSVersion() {
    return "ai.device.osVersion";
  }

  public String getDeviceLocale() {
    return "ai.device.locale";
  }

  public String getDeviceType() {
    return "ai.device.type";
  }

  public String getDeviceVMName() {
    return "ai.device.vmName";
  }

  public String getDeviceOEMName() {
    return "ai.device.oemName";
  }

  public String getDeviceModel() {
    return "ai.device.model";
  }

  public String getDeviceNetwork() {
    return "ai.device.network";
  }

  public String getDeviceScreenResolution() {
    return "ai.device.screenResolution";
  }

  public String getDeviceLanguage() {
    return "ai.device.language";
  }

  public String getDeviceIp() {
    return "ai.device.ip";
  }

  public String getLocationLatitude() {
    return "ai.location.latitude";
  }

  public String getLocationLongitude() {
    return "ai.location.longitude";
  }

  public String getLocationIP() {
    return "ai.location.ip";
  }

  public String getLocationContinent() {
    return "ai.location.continent";
  }

  public String getLocationCountry() {
    return "ai.location.country";
  }

  public String getLocationProvince() {
    return "ai.location.province";
  }

  public String getLocationCity() {
    return "ai.location.city";
  }

  public String getOperationName() {
    return "ai.operation.name";
  }

  public String getOperationId() {
    return "ai.operation.id";
  }

  public String getSyntheticSource() {
    return "ai.operation.syntheticSource";
  }

  public String getOperationParentId() {
    return "ai.operation.parentId";
  }

  public String getOperationRootId() {
    return "ai.operation.rootId";
  }

  public String getSessionId() {
    return "ai.session.id";
  }

  public String getSessionIsFirst() {
    return "ai.session.isFirst";
  }

  public String getSessionIsNew() {
    return "ai.session.isNew";
  }

  public String getUserType() {
    return "ai.user.type";
  }

  public String getUserId() {
    return "ai.user.id";
  }

  public String getUserAuthUserId() {
    return "ai.user.authUserId";
  }

  public String getUserAccountId() {
    return "ai.user.accountId";
  }

  public String getUserAnonymousUserAcquisitionDate() {
    return "ai.user.anonUserAcquisitionDate";
  }

  public String getUserAuthenticatedUserAcquisitionDate() {
    return "ai.user.authUserAcquisitionDate";
  }

  public String getUserAccountAcquisitionDate() {
    return "ai.user.accountAcquisitionDate";
  }

  public String getUserAgent() {
    return "ai.user.userAgent";
  }

  public String getSampleRate() {
    return "ai.sample.sampleRate";
  }

  public String getCloudRole() {
    return "ai.cloud.role";
  }

  public String getCloudRoleInstance() {
    return "ai.cloud.roleInstance";
  }

  public String getOperationCorrelationVector() {
    return "ai.operation.correlationVector";
  }

  private ContextTagKeys() {}
}
