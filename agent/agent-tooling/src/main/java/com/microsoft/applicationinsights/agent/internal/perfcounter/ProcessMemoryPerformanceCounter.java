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

package com.microsoft.applicationinsights.agent.internal.perfcounter;

import static com.microsoft.applicationinsights.agent.internal.perfcounter.MetricNames.PROCESS_MEMORY;

import com.microsoft.applicationinsights.agent.internal.telemetry.TelemetryClient;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The class supplies the memory usage in Mega Bytes of the Java process the SDK is in. */
public class ProcessMemoryPerformanceCounter implements PerformanceCounter {

  private static final Logger logger =
      LoggerFactory.getLogger(ProcessMemoryPerformanceCounter.class);

  public ProcessMemoryPerformanceCounter() {}

  @Override
  public void report(TelemetryClient telemetryClient) {
    MemoryMXBean memoryData = ManagementFactory.getMemoryMXBean();

    MemoryUsage heapMemoryUsage = memoryData.getHeapMemoryUsage();
    MemoryUsage nonHeapMemoryUsage = memoryData.getNonHeapMemoryUsage();

    double memoryBytes = (double) heapMemoryUsage.getUsed();
    memoryBytes += (double) nonHeapMemoryUsage.getUsed();

    logger.trace("Performance Counter: {}: {}", PROCESS_MEMORY, memoryBytes);
    telemetryClient.trackAsync(telemetryClient.newMetricTelemetry(PROCESS_MEMORY, memoryBytes));
  }
}
