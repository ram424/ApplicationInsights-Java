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

package com.microsoft.applicationinsights.smoketest.schemav2;

/** Data contract class DataPoint. */
public class DataPoint {
  /** Backing field for property Name. */
  private String name;

  /** Backing field for property Value. */
  private double value;

  /** Backing field for property Count. */
  private Integer count;

  /** Backing field for property Min. */
  private Double min;

  /** Backing field for property Max. */
  private Double max;

  /** Backing field for property StdDev. */
  private Double stdDev;

  /** Initializes a new instance of the DataPoint class. */
  public DataPoint() {}

  /** Gets the Name property. */
  public String getName() {
    return this.name;
  }

  /** Sets the Name property. */
  public void setName(String value) {
    this.name = value;
  }

  /** Gets the Value property. */
  public double getValue() {
    return this.value;
  }

  /** Sets the Value property. */
  public void setValue(double value) {
    this.value = value;
  }

  /** Gets the Count property. */
  public Integer getCount() {
    return this.count;
  }

  /** Sets the Count property. */
  public void setCount(Integer value) {
    this.count = value;
  }

  /** Gets the Min property. */
  public Double getMin() {
    return this.min;
  }

  /** Sets the Min property. */
  public void setMin(Double value) {
    this.min = value;
  }

  /** Gets the Max property. */
  public Double getMax() {
    return this.max;
  }

  /** Sets the Max property. */
  public void setMax(Double value) {
    this.max = value;
  }

  /** Gets the StdDev property. */
  public Double getStdDev() {
    return this.stdDev;
  }

  /** Sets the StdDev property. */
  public void setStdDev(Double value) {
    this.stdDev = value;
  }
}
