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

package com.microsoft.applicationinsights.alerting;

import static com.microsoft.applicationinsights.alerting.config.AlertMetricType.CPU;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.microsoft.applicationinsights.alerting.analysis.TimeSource;
import com.microsoft.applicationinsights.alerting.analysis.aggregations.RollingAverage;
import com.microsoft.applicationinsights.alerting.analysis.data.TelemetryDataPoint;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleConsumer;
import org.junit.jupiter.api.Test;

class RollingAverageTest {

  @Test
  void alertsConsumer() {
    AtomicReference<Double> called = new AtomicReference<>();
    DoubleConsumer consumer = called::set;
    TestTimeSource timeSource = new TestTimeSource();
    RollingAverage rollingAverage = new RollingAverage(60, timeSource, false);
    rollingAverage.setConsumer(consumer);

    for (int i = 0; i < 10; i++) {
      rollingAverage.update(createDataPoint(0.1));
      timeSource.increment(10000);
    }
    assertThat(called.get()).isNotNull();
  }

  @Test
  void givesCorrectValue() {
    AtomicReference<Double> called = new AtomicReference<>();
    DoubleConsumer consumer = called::set;
    TestTimeSource timeSource = new TestTimeSource();
    RollingAverage rollingAverage = new RollingAverage(60, timeSource, false);
    rollingAverage.setConsumer(consumer);

    for (int i = 0; i < 10; i++) {
      rollingAverage.update(createDataPoint(0.0));
      rollingAverage.update(createDataPoint(0.5));
      rollingAverage.update(createDataPoint(1.0));
      timeSource.increment(10000);
    }

    assertThat(called.get()).isEqualTo(0.5d);
  }

  @Test
  void throwsAwayDataOutsidePeriod() {
    AtomicReference<Double> called = new AtomicReference<>();
    DoubleConsumer consumer = called::set;

    TestTimeSource timeSource = new TestTimeSource();
    RollingAverage rollingAverage = new RollingAverage(120, timeSource, false);
    rollingAverage.setConsumer(consumer);

    for (int i = 0; i < 10; i++) {
      rollingAverage.update(createDataPoint(2.0));
    }
    timeSource.increment(10000);
    for (int i = 0; i < 10; i++) {
      rollingAverage.update(createDataPoint(1.0));
    }
    timeSource.increment(150000);
    for (int i = 0; i < 10; i++) {
      rollingAverage.update(createDataPoint(0.1));
    }
    timeSource.increment(10000);
    rollingAverage.update(createDataPoint(1.0));

    assertThat(rollingAverage.compute().getAsDouble()).isEqualTo(0.1d, within(0.0001d));

    assertThat(called.get()).isEqualTo(0.1d, within(0.0001d));
  }

  private static TelemetryDataPoint createDataPoint(double v) {
    return TelemetryDataPoint.create(CPU, TimeSource.DEFAULT.getNow(), CPU.name(), v);
  }
}
