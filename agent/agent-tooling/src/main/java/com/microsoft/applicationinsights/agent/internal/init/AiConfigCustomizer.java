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

package com.microsoft.applicationinsights.agent.internal.init;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.auto.service.AutoService;
import com.microsoft.applicationinsights.agent.internal.configuration.Configuration;
import com.microsoft.applicationinsights.agent.internal.legacyheaders.DelegatingPropagatorProvider;
import io.opentelemetry.javaagent.extension.config.ConfigPropertySource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@AutoService(ConfigPropertySource.class)
public class AiConfigCustomizer implements ConfigPropertySource {

  @Override
  public Map<String, String> getProperties() {
    Configuration config = FirstEntryPoint.getConfiguration();

    Map<String, String> properties = new HashMap<>();
    properties.put(
        "otel.micrometer.step.millis",
        Long.toString(SECONDS.toMillis(config.preview.metricIntervalSeconds)));

    enableInstrumentations(config, properties);

    if (!config.preview.captureControllerSpans) {
      properties.put(
          "otel.instrumentation.common.experimental.controller-telemetry.enabled", "false");
    }
    properties.put("otel.instrumentation.common.experimental.view-telemetry.enabled", "false");
    properties.put(
        "otel.instrumentation.messaging.experimental.receive-telemetry.enabled", "false");
    // this is needed to capture kafka.record.queue_time_ms
    properties.put("otel.instrumentation.kafka.experimental-span-attributes", "true");

    setHttpHeaderConfiguration(
        properties,
        "otel.instrumentation.http.capture-headers.server.request",
        config.preview.captureHttpServerHeaders.requestHeaders);
    setHttpHeaderConfiguration(
        properties,
        "otel.instrumentation.http.capture-headers.server.response",
        config.preview.captureHttpServerHeaders.responseHeaders);
    setHttpHeaderConfiguration(
        properties,
        "otel.instrumentation.http.capture-headers.client.request",
        config.preview.captureHttpClientHeaders.requestHeaders);
    setHttpHeaderConfiguration(
        properties,
        "otel.instrumentation.http.capture-headers.client.response",
        config.preview.captureHttpClientHeaders.responseHeaders);

    // enable capturing all mdc properties
    properties.put(
        "otel.instrumentation.logback-appender.experimental.capture-mdc-attributes", "*");
    properties.put("otel.instrumentation.log4j-appender.experimental.capture-mdc-attributes", "*");
    properties.put(
        "otel.instrumentation.log4j-appender.experimental.capture-context-data-attributes", "*");
    properties.put(
        "otel.instrumentation.jboss-logmanager.experimental.capture-mdc-attributes", "*");

    properties.put(
        "otel.instrumentation.log4j-appender.experimental.capture-map-message-attributes", "true");

    // enable thread.name
    properties.put("otel.instrumentation.java-util-logging.experimental-log-attributes", "true");
    properties.put("otel.instrumentation.jboss-logmanager.experimental-log-attributes", "true");
    properties.put("otel.instrumentation.log4j-appender.experimental-log-attributes", "true");
    properties.put("otel.instrumentation.logback-appender.experimental-log-attributes", "true");

    // custom instrumentation
    if (!config.preview.customInstrumentation.isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (Configuration.CustomInstrumentation customInstrumentation :
          config.preview.customInstrumentation) {
        if (sb.length() > 0) {
          sb.append(';');
        }
        sb.append(customInstrumentation.className);
        sb.append('[');
        sb.append(customInstrumentation.methodName);
        sb.append(']');
      }
      properties.put("otel.instrumentation.methods.include", sb.toString());
    }

    properties.put("otel.propagators", DelegatingPropagatorProvider.NAME);

    properties.put("otel.traces.sampler", DelegatingSamplerProvider.NAME);

    String tracesExporter = getProperty("otel.traces.exporter");
    if (tracesExporter == null) {
      // currently Application Insights Exporter has to be configured manually because it relies on
      // using a BatchSpanProcessor with queue size 1 due to live metrics (this will change in the
      // future)
      properties.put("otel.traces.exporter", "none");

      // TODO (trask) this can go away once new indexer is rolled out to gov clouds
      List<String> httpClientResponseHeaders = new ArrayList<>();
      httpClientResponseHeaders.add("request-context");
      httpClientResponseHeaders.addAll(config.preview.captureHttpClientHeaders.responseHeaders);
      setHttpHeaderConfiguration(
          properties,
          "otel.instrumentation.http.capture-headers.client.response",
          httpClientResponseHeaders);
    } else {
      properties.put("otel.traces.exporter", tracesExporter);
    }

    String metricsExporter = getProperty("otel.metrics.exporter");
    if (metricsExporter == null) {
      // the metrics exporter is configured later in this case
      properties.put("otel.metrics.exporter", "none");
    } else {
      properties.put("otel.metrics.exporter", metricsExporter);
    }

    String logsExporter = getProperty("otel.logs.exporter");
    if (logsExporter == null) {
      // the logs exporter is configured later in this case
      properties.put("otel.logs.exporter", "none");
    } else {
      properties.put("otel.logs.exporter", logsExporter);
    }

    if (config.role.name != null) {
      // in case using another exporter
      properties.put("otel.service.name", config.role.name);
    }

    return properties;
  }

  private static void enableInstrumentations(Configuration config, Map<String, String> properties) {
    properties.put("otel.instrumentation.common.default-enabled", "false");

    properties.put("otel.instrumentation.experimental.span-suppression-strategy", "client");

    // instrumentation that cannot be disabled (currently at least)

    properties.put("otel.instrumentation.ai-azure-functions.enabled", "true");
    properties.put("otel.instrumentation.ai-applicationinsights-web.enabled", "true");

    properties.put("otel.instrumentation.apache-httpasyncclient.enabled", "true");
    properties.put("otel.instrumentation.apache-httpclient.enabled", "true");
    properties.put("otel.instrumentation.async-http-client.enabled", "true");
    properties.put("otel.instrumentation.executor.enabled", "true");
    properties.put("otel.instrumentation.google-http-client.enabled", "true");
    properties.put("otel.instrumentation.grpc.enabled", "true");
    properties.put("otel.instrumentation.guava.enabled", "true");
    properties.put("otel.instrumentation.http-url-connection.enabled", "true");
    properties.put("otel.instrumentation.java-http-client.enabled", "true");
    properties.put("otel.instrumentation.java-util-logging.enabled", "true");
    properties.put("otel.instrumentation.jaxrs.enabled", "true");
    properties.put("otel.instrumentation.jaxrs-client.enabled", "true");
    properties.put("otel.instrumentation.jaxws.enabled", "true");

    properties.put("otel.instrumentation.jboss-logmanager-appender.enabled", "true");
    properties.put("otel.instrumentation.jboss-logmanager-mdc.enabled", "true");
    properties.put("otel.instrumentation.jetty.enabled", "true");
    properties.put("otel.instrumentation.jetty-httpclient.enabled", "true");
    properties.put("otel.instrumentation.kotlinx-coroutines.enabled", "true");
    properties.put("otel.instrumentation.liberty.enabled", "true");
    properties.put("otel.instrumentation.liberty-dispatcher.enabled", "true");
    properties.put("otel.instrumentation.log4j-appender.enabled", "true");
    properties.put("otel.instrumentation.logback-appender.enabled", "true");
    properties.put("otel.instrumentation.log4j-mdc.enabled", "true");
    properties.put("otel.instrumentation.log4j-context-data.enabled", "true");
    properties.put("otel.instrumentation.logback-mdc.enabled", "true");
    properties.put("otel.instrumentation.methods.enabled", "true");

    // not supporting netty-3.8 for now
    properties.put("otel.instrumentation.netty-4.0.enabled", "true");
    properties.put("otel.instrumentation.netty-4.1.enabled", "true");

    properties.put("otel.instrumentation.okhttp.enabled", "true");
    properties.put("otel.instrumentation.opentelemetry-extension-annotations.enabled", "true");
    properties.put(
        "otel.instrumentation.opentelemetry-instrumentation-annotations.enabled", "true");
    properties.put("otel.instrumentation.opentelemetry-api.enabled", "true");
    properties.put("otel.instrumentation.opentelemetry-instrumentation-api.enabled", "true");
    properties.put("otel.instrumentation.reactor.enabled", "true");
    properties.put("otel.instrumentation.reactor-netty.enabled", "true");
    properties.put("otel.instrumentation.rxjava.enabled", "true");

    properties.put("otel.instrumentation.servlet.enabled", "true");
    properties.put("otel.instrumentation.spring-core.enabled", "true");
    properties.put("otel.instrumentation.spring-web.enabled", "true");
    properties.put("otel.instrumentation.spring-webmvc.enabled", "true");
    properties.put("otel.instrumentation.spring-webflux.enabled", "true");
    properties.put("otel.instrumentation.tomcat.enabled", "true");
    properties.put("otel.instrumentation.undertow.enabled", "true");

    if (config.instrumentation.micrometer.enabled) {
      // TODO (heya) replace with below when updating to upstream micrometer
      properties.put("otel.instrumentation.ai-micrometer.enabled", "true");
      properties.put("otel.instrumentation.ai-actuator-metrics.enabled", "true");
      // properties.put("otel.instrumentation.micrometer.enabled", "true");
      // properties.put("otel.instrumentation.spring-boot-actuator-autoconfigure.enabled", "true");
    }
    if (config.instrumentation.azureSdk.enabled) {
      properties.put("otel.instrumentation.azure-core.enabled", "true");
    }
    if (config.instrumentation.cassandra.enabled) {
      properties.put("otel.instrumentation.cassandra.enabled", "true");
    }
    if (config.instrumentation.jdbc.enabled) {
      properties.put("otel.instrumentation.jdbc.enabled", "true");
    }
    if (config.instrumentation.jms.enabled) {
      properties.put("otel.instrumentation.jms.enabled", "true");
      properties.put("otel.instrumentation.spring-jms.enabled", "true");
    }
    if (config.instrumentation.kafka.enabled) {
      properties.put("otel.instrumentation.kafka.enabled", "true");
      properties.put("otel.instrumentation.spring-kafka.enabled", "true");
    }
    if (config.instrumentation.mongo.enabled) {
      properties.put("otel.instrumentation.mongo.enabled", "true");
    }
    if (config.instrumentation.quartz.enabled) {
      properties.put("otel.instrumentation.quartz.enabled", "true");
    }
    if (config.instrumentation.rabbitmq.enabled) {
      properties.put("otel.instrumentation.rabbitmq.enabled", "true");
      properties.put("otel.instrumentation.spring-rabbit.enabled", "true");
    }
    if (config.instrumentation.redis.enabled) {
      properties.put("otel.instrumentation.jedis.enabled", "true");
      properties.put("otel.instrumentation.lettuce.enabled", "true");
    }
    if (config.instrumentation.springScheduling.enabled) {
      properties.put("otel.instrumentation.spring-scheduling.enabled", "true");
    }
    if (config.preview.instrumentation.akka.enabled) {
      properties.put("otel.instrumentation.akka-actor.enabled", "true");
      properties.put("otel.instrumentation.akka-http.enabled", "true");
    }
    if (config.preview.instrumentation.play.enabled) {
      properties.put("otel.instrumentation.play-mvc.enabled", "true");
      properties.put("otel.instrumentation.play-ws.enabled", "true");
    }
    if (config.preview.instrumentation.apacheCamel.enabled) {
      properties.put("otel.instrumentation.apache-camel.enabled", "true");
    }
    if (config.preview.instrumentation.grizzly.enabled) {
      // note: grizzly instrumentation is off by default upstream
      properties.put("otel.instrumentation.grizzly.enabled", "true");
    }
    if (config.preview.instrumentation.springIntegration.enabled) {
      properties.put("otel.instrumentation.spring-integration.enabled", "true");
    }
    if (config.preview.instrumentation.vertx.enabled) {
      properties.put("otel.instrumentation.vertx.enabled", "true");
    }
    if (config.preview.instrumentation.jaxrsAnnotations.enabled) {
      properties.put("otel.instrumentation.jaxrs-1.0.enabled", "true");
      properties.put("otel.instrumentation.jaxrs-annotations.enabled", "true");
    }
  }

  private static void setHttpHeaderConfiguration(
      Map<String, String> properties, String propertyName, List<String> headers) {
    if (!headers.isEmpty()) {
      properties.put(propertyName, join(headers, ','));
    }
  }

  private static String getProperty(String propertyName) {
    String value = System.getProperty(propertyName);
    if (value != null) {
      return value;
    }
    String envVarName = propertyName.replace('.', '_').toUpperCase(Locale.ROOT);
    return System.getenv(envVarName);
  }

  private static <T> String join(List<T> values, char separator) {
    StringBuilder sb = new StringBuilder();
    for (Object val : values) {
      if (sb.length() > 0) {
        sb.append(separator);
      }
      sb.append(val);
    }
    return sb.toString();
  }
}
