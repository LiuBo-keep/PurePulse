package org.purepulse;

import java.util.List;
import org.purepulse.model.MonitorData;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;

/**
 * System monitoring service responsible for collecting real-time
 * hardware and operating system statistics.
 *
 * <p>
 * This class uses the OSHI (Operating System and Hardware Information)
 * library to retrieve low-level system metrics including:
 * </p>
 *
 * <ul>
 *     <li>CPU utilization</li>
 *     <li>Memory usage</li>
 *     <li>Network upload and download speed</li>
 *     <li>Disk usage statistics</li>
 * </ul>
 *
 * <p>
 * The collected information is aggregated into a {@link MonitorData}
 * object which acts as a snapshot of the current system status.
 * </p>
 *
 * <p>
 * The service is designed to be lightweight and is typically invoked
 * periodically by a background monitoring thread.
 * </p>
 *
 * <p>
 * Network throughput is calculated by measuring the difference in
 * transmitted and received bytes between two sampling points.
 * </p>
 *
 * @author aidan.liu
 * @version 1.0
 * @since 2026-03-10
 */
public class MonitorService {

  /**
   * Root entry point for accessing system and hardware information.
   */
  private final SystemInfo systemInfo = new SystemInfo();

  /**
   * Provides access to hardware components such as CPU,
   * memory, and network interfaces.
   */
  private final HardwareAbstractionLayer hardware = systemInfo.getHardware();

  /**
   * CPU processor abstraction used to retrieve CPU load metrics.
   */
  private final CentralProcessor cpu = hardware.getProcessor();

  /**
   * System memory abstraction used for memory statistics.
   */
  private final GlobalMemory memory = hardware.getMemory();

  /**
   * Previous CPU tick snapshot used to calculate CPU load
   * between two measurement intervals.
   */
  private long[] prevTicks = cpu.getSystemCpuLoadTicks();

  /**
   * Shared monitoring data container returned to the UI.
   */
  private final MonitorData data = new MonitorData();

  /**
   * Previously recorded received network bytes.
   * Used to calculate download speed.
   */
  private long lastRecv = 0;

  /**
   * Previously recorded transmitted network bytes.
   * Used to calculate upload speed.
   */
  private long lastSent = 0;

  /**
   * Timestamp of the last network measurement.
   */
  private long lastTime = System.currentTimeMillis();

  /**
   * Collects current system performance metrics.
   *
   * <p>
   * This method gathers information from various hardware
   * and operating system sources and updates the {@link MonitorData}
   * object with the latest values.
   * </p>
   *
   * <p>
   * The collected metrics include:
   * </p>
   *
   * <ul>
   *     <li>CPU usage percentage</li>
   *     <li>Memory usage (used and total)</li>
   *     <li>Network upload and download speed</li>
   *     <li>Total disk usage percentage</li>
   * </ul>
   *
   * <p>
   * Network speeds are calculated using the difference between
   * the current byte counters and the previous sampling values.
   * </p>
   *
   * @return {@link MonitorData} object containing the latest system statistics
   */
  public MonitorData collect() {

    // 1. CPU usage calculation
    data.setCpuUsage(cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100);
    prevTicks = cpu.getSystemCpuLoadTicks();

    // 2. Memory statistics
    long totalMem = memory.getTotal();
    long availMem = memory.getAvailable();
    data.setMemTotal(totalMem / 1024 / 1024 / 1024);
    data.setMemUsed((totalMem - availMem) / 1024 / 1024 / 1024);

    // 3. Network speed calculation (download & upload)
    List<NetworkIF> networkIFs = hardware.getNetworkIFs();
    long currentRecv = 0;
    long currentSent = 0;

    for (NetworkIF net : networkIFs) {
      // Important: refresh interface statistics
      net.updateAttributes();
      currentRecv += net.getBytesRecv();
      currentSent += net.getBytesSent();
    }

    long currentTime = System.currentTimeMillis();
    double timeDiffSec = (currentTime - lastTime) / 1000.0;

    if (timeDiffSec > 0 && lastTime != 0) {
      // Convert bytes/sec to KB/sec
      data.setDownSpeed(((currentRecv - lastRecv) / 1024.0) / timeDiffSec);
      data.setUpSpeed(((currentSent - lastSent) / 1024.0) / timeDiffSec);
    }

    lastRecv = currentRecv;
    lastSent = currentSent;
    lastTime = currentTime;

    // 4. Disk usage statistics (aggregate across all file systems)
    List<OSFileStore> fileStores = systemInfo.getOperatingSystem().getFileSystem().getFileStores();
    long totalDiskSpace = 0;
    long usableDiskSpace = 0;

    for (OSFileStore fs : fileStores) {
      totalDiskSpace += fs.getTotalSpace();
      usableDiskSpace += fs.getUsableSpace();
    }

    if (totalDiskSpace > 0) {
      data.setDiskUsage((double) (totalDiskSpace - usableDiskSpace) / totalDiskSpace * 100);
    }

    return data;
  }
}
