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

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public final class Strings {

  public static boolean isNullOrEmpty(@Nullable String string) {
    return string == null || string.isEmpty();
  }

  @Nullable
  public static String trimAndEmptyToNull(@Nullable String str) {
    if (str == null) {
      return null;
    }
    String trimmed = str.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  public static Map<String, String> splitToMap(String str) {
    Map<String, String> map = new HashMap<>();
    for (String part : str.split(";")) {
      if (part.trim().isEmpty()) {
        continue;
      }
      int index = part.indexOf('=');
      if (index == -1) {
        throw new IllegalArgumentException();
      }
      String key = part.substring(0, index);
      String value = part.substring(index + 1);
      map.put(key, value);
    }
    return map;
  }

  private Strings() {}
}
