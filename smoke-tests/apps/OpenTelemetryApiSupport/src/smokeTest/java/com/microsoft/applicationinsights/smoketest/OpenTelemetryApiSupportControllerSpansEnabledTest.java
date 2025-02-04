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

package com.microsoft.applicationinsights.smoketest;

import static com.microsoft.applicationinsights.smoketest.WarEnvironmentValue.TOMCAT_8_JAVA_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.applicationinsights.smoketest.schemav2.Envelope;
import com.microsoft.applicationinsights.smoketest.schemav2.RemoteDependencyData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@Environment(TOMCAT_8_JAVA_8)
@UseAgent("controller_spans_enabled_applicationinsights.json")
class OpenTelemetryApiSupportControllerSpansEnabledTest {

  @RegisterExtension static final SmokeTestExtension testing = new SmokeTestExtension();

  @Test
  @TargetUri("/test-api")
  void testApi() throws Exception {
    Telemetry telemetry = testing.getTelemetry(1);

    assertThat(telemetry.rd.getName()).isEqualTo("GET /OpenTelemetryApiSupport/test-api");
    assertThat(telemetry.rd.getUrl())
        .matches("http://localhost:[0-9]+/OpenTelemetryApiSupport/test-api");
    assertThat(telemetry.rd.getResponseCode()).isEqualTo("200");
    assertThat(telemetry.rd.getSuccess()).isTrue();
    assertThat(telemetry.rd.getSource()).isNull();
    assertThat(telemetry.rd.getProperties()).isEmpty();
    assertThat(telemetry.rd.getMeasurements()).isEmpty();

    assertThat(telemetry.rdd1.getName()).isEqualTo("myspanname");
    assertThat(telemetry.rdd1.getData()).isNull();
    assertThat(telemetry.rdd1.getType()).isEqualTo("InProc");
    assertThat(telemetry.rdd1.getTarget()).isNull();
    assertThat(telemetry.rdd1.getProperties()).hasSize(2);
    assertThat(telemetry.rdd1.getProperties()).containsEntry("myattr1", "myvalue1");
    assertThat(telemetry.rdd1.getProperties()).containsEntry("myattr2", "myvalue2");
    assertThat(telemetry.rdd1.getSuccess()).isTrue();

    // ideally want the properties below on rd, but can't get SERVER span yet
    // see
    // https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/1726#issuecomment-731890267

    // checking that instrumentation key, cloud role name, cloud role instance, and sdk version are
    // from the agent
    assertThat(telemetry.rddEnvelope1.getIKey()).isEqualTo("00000000-0000-0000-0000-0FEEDDADBEEF");
    assertThat(telemetry.rddEnvelope1.getTags()).containsEntry("ai.cloud.role", "testrolename");
    assertThat(telemetry.rddEnvelope1.getTags().get("ai.cloud.roleInstance"))
        .isEqualTo("testroleinstance");
    assertThat(telemetry.rddEnvelope1.getTags())
        .hasEntrySatisfying("ai.internal.sdkVersion", v -> assertThat(v).startsWith("java:3."));
    assertThat(telemetry.rddEnvelope1.getTags()).containsEntry("ai.user.id", "myuser");

    SmokeTestExtension.assertParentChild(
        telemetry.rd,
        telemetry.rdEnvelope,
        telemetry.rddEnvelope1,
        "GET /OpenTelemetryApiSupport/test-api");
  }

  @Test
  @TargetUri("/test-overriding-ikey-etc")
  void testOverridingIkeyEtc() throws Exception {
    Telemetry telemetry = testing.getTelemetry(1);

    assertThat(telemetry.rd.getName())
        .isEqualTo("GET /OpenTelemetryApiSupport/test-overriding-ikey-etc");
    assertThat(telemetry.rd.getUrl())
        .matches("http://localhost:[0-9]+/OpenTelemetryApiSupport/test-overriding-ikey-etc");
    assertThat(telemetry.rd.getResponseCode()).isEqualTo("200");
    assertThat(telemetry.rd.getSuccess()).isTrue();
    assertThat(telemetry.rd.getSource()).isNull();
    assertThat(telemetry.rd.getProperties()).isEmpty();
    assertThat(telemetry.rd.getMeasurements()).isEmpty();

    assertThat(telemetry.rdd1.getName()).isEqualTo("TestController.testOverridingIkeyEtc");
    assertThat(telemetry.rdd1.getData()).isNull();
    assertThat(telemetry.rdd1.getType()).isEqualTo("InProc");
    assertThat(telemetry.rdd1.getTarget()).isNull();
    assertThat(telemetry.rdd1.getProperties()).isEmpty();
    assertThat(telemetry.rdd1.getSuccess()).isTrue();

    // ideally want the properties below on rd, but can't get SERVER span yet, see
    // https://github.com/open-telemetry/opentelemetry-java-instrumentation/issues/1726#issuecomment-731890267

    // checking that instrumentation key, cloud role name, cloud role instance, and sdk version are
    // from the agent
    assertThat(telemetry.rddEnvelope1.getIKey()).isEqualTo("12341234-1234-1234-1234-123412341234");
    assertThat(telemetry.rddEnvelope1.getTags()).containsEntry("ai.cloud.role", "role-name-here");
    assertThat(telemetry.rddEnvelope1.getTags())
        .containsEntry("ai.cloud.roleInstance", "role-instance-here");
    assertThat(telemetry.rddEnvelope1.getTags())
        .containsEntry("ai.application.ver", "application-version-here");
    assertThat(telemetry.rddEnvelope1.getTags())
        .hasEntrySatisfying("ai.internal.sdkVersion", v -> assertThat(v).startsWith("java:3."));

    SmokeTestExtension.assertParentChild(
        telemetry.rd,
        telemetry.rdEnvelope,
        telemetry.rddEnvelope1,
        "GET /OpenTelemetryApiSupport/test-overriding-ikey-etc");
  }

  @Test
  @TargetUri("/test-annotations")
  void testAnnotations() throws Exception {
    Telemetry telemetry = testing.getTelemetry(2);

    if (!telemetry.rdd1.getName().equals("TestController.testAnnotations")) {
      RemoteDependencyData rddTemp = telemetry.rdd1;
      telemetry.rdd1 = telemetry.rdd2;
      telemetry.rdd2 = rddTemp;

      Envelope rddEnvelopeTemp = telemetry.rddEnvelope1;
      telemetry.rddEnvelope1 = telemetry.rddEnvelope2;
      telemetry.rddEnvelope2 = rddEnvelopeTemp;
    }

    assertThat(telemetry.rd.getName()).isEqualTo("GET /OpenTelemetryApiSupport/test-annotations");
    assertThat(telemetry.rd.getUrl())
        .matches("http://localhost:[0-9]+/OpenTelemetryApiSupport/test-annotations");
    assertThat(telemetry.rd.getResponseCode()).isEqualTo("200");
    assertThat(telemetry.rd.getSuccess()).isTrue();
    assertThat(telemetry.rd.getSource()).isNull();
    assertThat(telemetry.rd.getProperties()).isEmpty();
    assertThat(telemetry.rd.getMeasurements()).isEmpty();

    assertThat(telemetry.rdd1.getName()).isEqualTo("TestController.testAnnotations");
    assertThat(telemetry.rdd1.getData()).isNull();
    assertThat(telemetry.rdd1.getType()).isEqualTo("InProc");
    assertThat(telemetry.rdd1.getTarget()).isNull();
    assertThat(telemetry.rdd1.getProperties()).isEmpty();
    assertThat(telemetry.rdd1.getSuccess()).isTrue();

    assertThat(telemetry.rdd2.getName()).isEqualTo("TestController.underAnnotation");
    assertThat(telemetry.rdd2.getData()).isNull();
    assertThat(telemetry.rdd2.getType()).isEqualTo("InProc");
    assertThat(telemetry.rdd2.getTarget()).isNull();
    assertThat(telemetry.rdd2.getProperties()).containsEntry("message", "a message");
    assertThat(telemetry.rdd2.getProperties()).hasSize(1);
    assertThat(telemetry.rdd2.getSuccess()).isTrue();

    SmokeTestExtension.assertParentChild(
        telemetry.rd,
        telemetry.rdEnvelope,
        telemetry.rddEnvelope1,
        "GET /OpenTelemetryApiSupport/test-annotations");
    SmokeTestExtension.assertParentChild(
        telemetry.rdd1,
        telemetry.rddEnvelope1,
        telemetry.rddEnvelope2,
        "GET /OpenTelemetryApiSupport/test-annotations");
  }
}
