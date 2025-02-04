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

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Stack frame information. */
@Fluent
public final class StackFrame {
  /*
   * The level property.
   */
  @JsonProperty(value = "level", required = true)
  private int level;

  /*
   * Method name.
   */
  @JsonProperty(value = "method", required = true)
  private String method;

  /*
   * Name of the assembly (dll, jar, etc.) containing this function.
   */
  @JsonProperty(value = "assembly")
  private String assembly;

  /*
   * File name or URL of the method implementation.
   */
  @JsonProperty(value = "fileName")
  private String fileName;

  /*
   * Line number of the code implementation.
   */
  @JsonProperty(value = "line")
  private Integer line;

  /**
   * Get the level property: The level property.
   *
   * @return the level value.
   */
  public int getLevel() {
    return this.level;
  }

  /**
   * Set the level property: The level property.
   *
   * @param level the level value to set.
   * @return the StackFrame object itself.
   */
  public StackFrame setLevel(int level) {
    this.level = level;
    return this;
  }

  /**
   * Get the method property: Method name.
   *
   * @return the method value.
   */
  public String getMethod() {
    return this.method;
  }

  /**
   * Set the method property: Method name.
   *
   * @param method the method value to set.
   * @return the StackFrame object itself.
   */
  public StackFrame setMethod(String method) {
    this.method = method;
    return this;
  }

  /**
   * Get the assembly property: Name of the assembly (dll, jar, etc.) containing this function.
   *
   * @return the assembly value.
   */
  public String getAssembly() {
    return this.assembly;
  }

  /**
   * Set the assembly property: Name of the assembly (dll, jar, etc.) containing this function.
   *
   * @param assembly the assembly value to set.
   * @return the StackFrame object itself.
   */
  public StackFrame setAssembly(String assembly) {
    this.assembly = assembly;
    return this;
  }

  /**
   * Get the fileName property: File name or URL of the method implementation.
   *
   * @return the fileName value.
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * Set the fileName property: File name or URL of the method implementation.
   *
   * @param fileName the fileName value to set.
   * @return the StackFrame object itself.
   */
  public StackFrame setFileName(String fileName) {
    this.fileName = fileName;
    return this;
  }

  /**
   * Get the line property: Line number of the code implementation.
   *
   * @return the line value.
   */
  public Integer getLine() {
    return this.line;
  }

  /**
   * Set the line property: Line number of the code implementation.
   *
   * @param line the line value to set.
   * @return the StackFrame object itself.
   */
  public StackFrame setLine(Integer line) {
    this.line = line;
    return this;
  }
}
