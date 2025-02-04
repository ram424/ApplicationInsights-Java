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

package com.azure.monitor.opentelemetry.exporter;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.azure.core.util.Configuration;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.EventData;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.data.StatusData;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AzureMonitorTraceExporter}. */
public class AzureMonitorTraceExporterTest extends MonitorExporterClientTestBase {

  private static final String TRACE_ID = TraceId.fromLongs(10L, 2L);
  private static final String SPAN_ID = SpanId.fromLong(1);
  private static final TraceState TRACE_STATE = TraceState.builder().build();

  @Test
  public void testExportRequestData() {
    String connectionStringTemplate =
        "InstrumentationKey=ikey;IngestionEndpoint=https://testendpoint.com";
    String connectionString =
        Configuration.getGlobalConfiguration()
            .get("APPLICATIONINSIGHTS_CONNECTION_STRING", connectionStringTemplate);
    AzureMonitorTraceExporter azureMonitorTraceExporter =
        getClientBuilder().connectionString(connectionString).buildTraceExporter();
    CompletableResultCode export =
        azureMonitorTraceExporter.export(Collections.singleton(new RequestSpanData()));
    export.join(30, TimeUnit.SECONDS);
    Assertions.assertTrue(export.isDone());
    Assertions.assertTrue(export.isSuccess());
  }

  static class RequestSpanData implements SpanData {

    @Override
    public SpanContext getSpanContext() {
      return SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE);
    }

    @Override
    public String getTraceId() {
      return TRACE_ID;
    }

    @Override
    public String getSpanId() {
      return SPAN_ID;
    }

    @Override
    public SpanContext getParentSpanContext() {
      return SpanContext.create(TRACE_ID, SPAN_ID, TraceFlags.getDefault(), TRACE_STATE);
    }

    @Override
    public String getParentSpanId() {
      return SpanId.fromLong(1);
    }

    @Override
    public Resource getResource() {
      return Resource.create(Attributes.empty());
    }

    @Override
    public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
      return InstrumentationLibraryInfo.create("TestLib", "1");
    }

    @Override
    public InstrumentationScopeInfo getInstrumentationScopeInfo() {
      return InstrumentationScopeInfo.create("TestLib", "1", null);
    }

    @Override
    public String getName() {
      return "/service/resource";
    }

    @Override
    public SpanKind getKind() {
      return SpanKind.INTERNAL;
    }

    @Override
    public long getStartEpochNanos() {
      return MILLISECONDS.toNanos(Instant.now().toEpochMilli());
    }

    @Override
    public Attributes getAttributes() {
      return Attributes.builder()
          .put("http.status_code", 200L)
          .put("http.url", "http://localhost")
          .put("http.method", "GET")
          .put("ai.sampling.percentage", 100.0)
          .build();
    }

    @Override
    public List<EventData> getEvents() {
      return new ArrayList<>();
    }

    @Override
    public List<LinkData> getLinks() {
      return new ArrayList<>();
    }

    @Override
    public StatusData getStatus() {
      return StatusData.ok();
    }

    @Override
    public long getEndEpochNanos() {
      return MILLISECONDS.toNanos(Instant.now().toEpochMilli());
    }

    @Override
    public boolean hasEnded() {
      return false;
    }

    @Override
    public int getTotalRecordedEvents() {
      return 0;
    }

    @Override
    public int getTotalRecordedLinks() {
      return 0;
    }

    @Override
    public int getTotalAttributeCount() {
      return 0;
    }
  }
}
