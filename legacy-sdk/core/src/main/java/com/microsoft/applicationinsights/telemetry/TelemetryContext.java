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

package com.microsoft.applicationinsights.telemetry;

import com.microsoft.applicationinsights.extensibility.context.CloudContext;
import com.microsoft.applicationinsights.extensibility.context.ComponentContext;
import com.microsoft.applicationinsights.extensibility.context.DeviceContext;
import com.microsoft.applicationinsights.extensibility.context.LocationContext;
import com.microsoft.applicationinsights.extensibility.context.OperationContext;
import com.microsoft.applicationinsights.extensibility.context.SessionContext;
import com.microsoft.applicationinsights.extensibility.context.UserContext;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a context for sending telemetry to the Application Insights service. The context holds
 * data that is sent with every telemetry item. It includes the instrumentation key; the current
 * operation id, for correlating related telemetry items; and user, session and device data. You can
 * also set properties that are added to every telemetry item, and can be used in the portal to
 * filter the telemetry that used this context.
 */
public final class TelemetryContext {

  private final ConcurrentMap<String, String> properties;
  private final ContextTagsMap tags;

  private String instrumentationKey;
  private ComponentContext component;
  private DeviceContext device;
  private SessionContext session;
  private UserContext user;
  private OperationContext operation;
  private LocationContext location;
  private CloudContext cloud;

  public TelemetryContext() {
    this(new ConcurrentHashMap<>(), new ContextTagsMap());
  }

  TelemetryContext(ConcurrentMap<String, String> properties, ContextTagsMap tags) {
    if (properties == null) {
      throw new IllegalArgumentException("properties cannot be null");
    }
    if (tags == null) {
      throw new IllegalArgumentException("tags cannot be null");
    }
    this.properties = properties;
    this.tags = tags;
  }

  /** Gets the object describing the component (application) tracked by this instance. */
  public ComponentContext getComponent() {
    if (component == null) {
      component = new ComponentContext(tags);
    }
    return component;
  }

  /** Gets the object describing the device tracked by this instance. */
  public DeviceContext getDevice() {
    if (device == null) {
      device = new DeviceContext(tags);
    }
    return device;
  }

  /** Gets the object describing a user session tracked by this instance. */
  public SessionContext getSession() {
    if (session == null) {
      session = new SessionContext(tags);
    }
    return session;
  }

  /** Gets the object describing a user tracked by this instance. */
  public UserContext getUser() {
    if (user == null) {
      user = new UserContext(tags);
    }
    return user;
  }

  /**
   * Gets the current operation (typically an HTTP request). Used to correlate events - for example,
   * exceptions generated while processing a request.
   */
  public OperationContext getOperation() {
    if (operation == null) {
      operation = new OperationContext(tags);
    }
    return operation;
  }

  /** Gets the object describing a location tracked by this instance. */
  public LocationContext getLocation() {
    if (location == null) {
      location = new LocationContext(tags);
    }
    return location;
  }

  /** Gets the object describing the role and instance in the cloud. */
  public CloudContext getCloud() {
    if (cloud == null) {
      cloud = new CloudContext(tags);
    }
    return cloud;
  }

  /**
   * Gets the default instrumentation key for all {@link
   * com.microsoft.applicationinsights.telemetry.Telemetry} objects logged in this {@link
   * com.microsoft.applicationinsights.telemetry.TelemetryContext}.
   *
   * <p>You can specify it for all telemetry tracked via a particular {@link
   * com.microsoft.applicationinsights.TelemetryClient} or for a specific {@link
   * com.microsoft.applicationinsights.telemetry.Telemetry}
   *
   * @return The instrumentation key
   */
  public String getInstrumentationKey() {
    return instrumentationKey;
  }

  /**
   * Sets the default instrumentation key for all {@link
   * com.microsoft.applicationinsights.telemetry.Telemetry} objects logged in this {@link
   * com.microsoft.applicationinsights.telemetry.TelemetryContext}.
   *
   * <p>You can specify it for all telemetry tracked via a particular {@link
   * com.microsoft.applicationinsights.TelemetryClient} or for a specific {@link
   * com.microsoft.applicationinsights.telemetry.Telemetry}
   *
   * @param instrumentationKey The instrumentation key
   */
  public void setInstrumentationKey(String instrumentationKey) {
    this.instrumentationKey = instrumentationKey;
  }

  /** Gets a dictionary of application-defined property values. */
  public ConcurrentMap<String, String> getProperties() {
    return properties;
  }

  /** Gets a dictionary of context tags. */
  public ConcurrentMap<String, String> getTags() {
    return tags;
  }
}
