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

package com.microsoft.applicationinsights.serviceprofilerapi.profiler;

import com.microsoft.applicationinsights.alerting.alert.AlertBreach;
import com.microsoft.applicationinsights.alerting.config.AlertMetricType;
import com.microsoft.applicationinsights.alerting.config.AlertingConfiguration.AlertConfiguration;
import com.microsoft.applicationinsights.profiler.ProfileHandler;
import com.microsoft.applicationinsights.profiler.Profiler;
import com.microsoft.applicationinsights.profiler.ProfilerConfiguration;
import com.microsoft.applicationinsights.profiler.ProfilerConfigurationHandler;
import com.microsoft.applicationinsights.profiler.config.ServiceProfilerServiceConfig;
import com.microsoft.applicationinsights.profiler.uploader.UploadCompleteHandler;
import com.microsoft.jfr.FlightRecorderConnection;
import com.microsoft.jfr.JfrStreamingException;
import com.microsoft.jfr.Recording;
import com.microsoft.jfr.RecordingConfiguration;
import com.microsoft.jfr.RecordingOptions;
import com.microsoft.jfr.dcmd.FlightRecorderDiagnosticCommandConnection;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages connecting JFR interaction.
 *
 * <ul>
 *   <li>Instantiates FlightRecorder subsystem
 *   <li>Creates profiles on demand
 * </ul>
 */
public class JfrProfiler implements ProfilerConfigurationHandler, Profiler {
  private static final Logger LOGGER = LoggerFactory.getLogger(JfrProfiler.class);

  // service execution context
  private ScheduledExecutorService scheduledExecutorService;

  // Action to perform when a profile has been created
  private ProfileHandler profileHandler;

  private FlightRecorderConnection flightRecorderConnection;
  private RecordingOptions.Builder recordingOptionsBuilder;

  private final AlertConfiguration periodicConfig;

  private final Object activeRecordingLock = new Object();
  @Nullable private Recording activeRecording = null;
  @Nullable private File activeRecordingFile = null;

  private final RecordingConfiguration memoryRecordingConfiguration;
  private final RecordingConfiguration cpuRecordingConfiguration;
  private final RecordingConfiguration spanRecordingConfiguration;

  private final File temporaryDirectory;

  public JfrProfiler(ServiceProfilerServiceConfig configuration) {
    periodicConfig =
        new AlertConfiguration(
            AlertMetricType.PERIODIC,
            false,
            0.0f,
            configuration.getPeriodicRecordingDuration(),
            configuration.getPeriodicRecordingInterval());

    memoryRecordingConfiguration =
        AlternativeJfrConfigurations.getMemoryProfileConfig(configuration);
    cpuRecordingConfiguration = AlternativeJfrConfigurations.getCpuProfileConfig(configuration);
    spanRecordingConfiguration = AlternativeJfrConfigurations.getSpanProfileConfig(configuration);
    temporaryDirectory = configuration.tempDirectory();
  }

  /**
   * Call init before run.
   *
   * @throws IOException Trouble communicating with MBean server
   * @throws InstanceNotFoundException The JVM does not support JFR, or experimental option is not
   *     enabled.
   */
  @Override
  public boolean initialize(
      ProfileHandler profileHandler, ScheduledExecutorService scheduledExecutorService)
      throws IOException, InstanceNotFoundException {
    this.profileHandler = profileHandler;
    this.scheduledExecutorService = scheduledExecutorService;

    // TODO -  allow user configuration of profile options
    recordingOptionsBuilder = new RecordingOptions.Builder();

    try {
      // connect to mbeans
      MBeanServerConnection mbeanServer = ManagementFactory.getPlatformMBeanServer();
      try {
        flightRecorderConnection = FlightRecorderConnection.connect(mbeanServer);
      } catch (JfrStreamingException | InstanceNotFoundException jfrStreamingException) {
        // Possibly an older JVM, try using Diagnostic command
        flightRecorderConnection = FlightRecorderDiagnosticCommandConnection.connect(mbeanServer);
      }
    } catch (Exception e) {
      LOGGER.error("Failed to connect to mbean", e);
      return false;
    }

    return true;
  }

  /** Apply new configuration settings obtained from Service Profiler. */
  @Override
  public void updateConfiguration(ProfilerConfiguration newConfig) {
    LOGGER.debug("Received config {}", newConfig.getLastModified());

    // TODO update periodic profile configuration
  }

  protected void profileAndUpload(
      AlertBreach alertBreach, Duration duration, UploadCompleteHandler uploadCompleteHandler) {
    Instant recordingStart = Instant.now();
    executeProfile(
        alertBreach.getType(),
        duration,
        uploadNewRecording(alertBreach, recordingStart, uploadCompleteHandler));
  }

  @Nullable
  protected Recording startRecording(AlertMetricType alertType, Duration duration) {
    synchronized (activeRecordingLock) {
      if (activeRecording != null) {
        LOGGER.warn("Alert received, however a profile is already in progress, ignoring request.");
        return null;
      }

      RecordingConfiguration recordingConfiguration;
      switch (alertType) {
        case CPU:
          recordingConfiguration = cpuRecordingConfiguration;
          break;
        case REQUEST:
          recordingConfiguration = spanRecordingConfiguration;
          break;
        case MEMORY:
          recordingConfiguration = memoryRecordingConfiguration;
          break;
        default:
          recordingConfiguration = RecordingConfiguration.PROFILE_CONFIGURATION;
      }

      try {
        activeRecordingFile = createJfrFile(duration);

        // As a fallback in case recording closing logic does not succeed, set the recording
        // duration to the expected duration plus 60 seconds
        Duration requestedDuration = duration.plus(60, ChronoUnit.SECONDS);

        RecordingOptions recordingOptions =
            recordingOptionsBuilder.duration(requestedDuration.toMillis() + " ms").build();

        this.activeRecording = createRecording(recordingOptions, recordingConfiguration);

        return activeRecording;
      } catch (IOException e) {
        LOGGER.error("Failed to create jfr file", e);
        return null;
      }
    }
  }

  protected Recording createRecording(
      RecordingOptions recordingOptions, RecordingConfiguration recordingConfiguration) {
    return flightRecorderConnection.newRecording(recordingOptions, recordingConfiguration);
  }

  /** Perform a profile and notify the handler. */
  protected void executeProfile(
      AlertMetricType alertType, Duration duration, Consumer<Recording> handler) {

    LOGGER.info("Received " + alertType + " alert, Starting profile");

    if (flightRecorderConnection == null) {
      LOGGER.error("Flight recorder not initialised");
      return;
    }

    Recording newRecording = startRecording(alertType, duration);

    if (newRecording == null) {
      return;
    }

    try {
      newRecording.start();

      // schedule closing the recording
      scheduledExecutorService.schedule(
          () -> handler.accept(newRecording), duration.getSeconds(), TimeUnit.SECONDS);

    } catch (IOException ioException) {
      LOGGER.error("Failed to start JFR recording", ioException);
      CompletableFuture<?> future = new CompletableFuture<>();
      future.completeExceptionally(ioException);
    } catch (JfrStreamingException internalError) {
      LOGGER.error("Internal JFR Error", internalError);
      CompletableFuture<?> future = new CompletableFuture<>();
      future.completeExceptionally(internalError);
    }
  }

  /** When a profile has been created, upload it to service profiler. */
  @SuppressWarnings("CatchingUnchecked")
  protected Consumer<Recording> uploadNewRecording(
      AlertBreach alertBreach,
      Instant recordingStart,
      UploadCompleteHandler uploadCompleteHandler) {
    return recording -> {
      LOGGER.info("Closing and uploading recording");
      try {
        // dump profile to file
        closeRecording(activeRecording, activeRecordingFile);

        // notify handler of a new profile
        profileHandler.receive(
            alertBreach, recordingStart.toEpochMilli(), activeRecordingFile, uploadCompleteHandler);

      } catch (Exception e) {
        LOGGER.error("Failed to upload recording", e);
      } catch (Error e) {
        // rethrow errors
        LOGGER.error("Failed to upload recording", e);
        throw e;
      } finally {
        clearActiveRecording();
      }
    };
  }

  private static void closeRecording(Recording recording, File recordingFile) {
    try {
      // close recording
      recording.dump(recordingFile.getAbsolutePath());
    } catch (IOException e) {
      LOGGER.error("Failed to close recording", e);
    } catch (JfrStreamingException internalError) {
      // Sometimes the  mbean dump fails...Try alternative of streaming data out
      try {
        writeFileFromStream(recording, recordingFile);
      } catch (IOException e) {
        LOGGER.error("Failed to close recording", e);
      } catch (JfrStreamingException e) {
        LOGGER.error("Internal JFR Error", e);
      }
    } finally {
      try {
        recording.close();
      } catch (IOException e) {
        LOGGER.error("Failed to close recording", e);
      }
    }
  }

  private static void writeFileFromStream(Recording recording, File recordingFile)
      throws IOException, JfrStreamingException {
    if (recordingFile.exists()) {
      recordingFile.delete();
    }
    recordingFile.createNewFile();

    try (BufferedInputStream stream = new BufferedInputStream(recording.getStream(null, null));
        FileOutputStream fos = new FileOutputStream(recordingFile)) {
      int read;
      byte[] buffer = new byte[10 * 1024];
      while ((read = stream.read(buffer)) != -1) {
        fos.write(buffer, 0, read);
      }
    }
  }

  private void clearActiveRecording() {
    synchronized (activeRecordingLock) {
      activeRecording = null;

      // delete uploaded profile
      if (activeRecordingFile != null && activeRecordingFile.exists()) {
        if (!activeRecordingFile.delete()) {
          LOGGER.error("Failed to remove file " + activeRecordingFile.getAbsolutePath());
        }
      }
      activeRecordingFile = null;
    }
  }

  /** Dump JFR profile to file. */
  protected File createJfrFile(Duration duration) throws IOException {
    if (!temporaryDirectory.exists()) {
      if (!temporaryDirectory.mkdirs()) {
        throw new IOException(
            "Failed to create temporary directory " + temporaryDirectory.getAbsolutePath());
      }
    }

    Instant recordingStart = Instant.now();
    Instant recordingEnd = recordingStart.plus(duration);

    return new File(
        temporaryDirectory,
        "recording_" + recordingStart.toEpochMilli() + "-" + recordingEnd.toEpochMilli() + ".jfr");
  }

  /** Action to be performed on a periodic profile request. */
  public void performPeriodicProfile(UploadCompleteHandler uploadCompleteHandler) {
    LOGGER.info("Received periodic profile request");
    AlertBreach breach =
        new AlertBreach(AlertMetricType.PERIODIC, 0, periodicConfig, UUID.randomUUID().toString());
    profileAndUpload(
        breach,
        Duration.ofSeconds(breach.getAlertConfiguration().getProfileDuration()),
        uploadCompleteHandler);
  }

  /** Dispatch alert breach event to handler. */
  @Override
  public void accept(AlertBreach alertBreach, UploadCompleteHandler uploadCompleteHandler) {

    if (alertBreach.getType() == AlertMetricType.PERIODIC) {
      performPeriodicProfile(uploadCompleteHandler);
    } else {
      profileAndUpload(
          alertBreach,
          Duration.ofSeconds(alertBreach.getAlertConfiguration().getProfileDuration()),
          uploadCompleteHandler);
    }
  }
}
