package org.purepulse;

import java.util.List;
import org.purepulse.model.MonitorData;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OSFileStore;

public class MonitorService {
  private final SystemInfo systemInfo = new SystemInfo();
  private final HardwareAbstractionLayer hardware = systemInfo.getHardware();
  private final CentralProcessor cpu = hardware.getProcessor();
  private final GlobalMemory memory = hardware.getMemory();
  private long[] prevTicks = cpu.getSystemCpuLoadTicks();

  // 用于网速计算的辅助变量
  private long lastRecv = 0;
  private long lastSent = 0;
  private long lastTime = System.currentTimeMillis();

  public MonitorData collect() {
    MonitorData data = new MonitorData();

    // 1. CPU 使用率
    data.cpuUsage = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
    prevTicks = cpu.getSystemCpuLoadTicks();

    // 2. CPU 温度
    data.cpuTemp = hardware.getSensors().getCpuTemperature();

    // 3. 内存统计
    long totalMem = memory.getTotal();
    long availMem = memory.getAvailable();
    data.memTotal = totalMem / 1024 / 1024 / 1024;
    data.memUsed = (totalMem - availMem) / 1024 / 1024 / 1024;

    // 4. 网速计算 (下载 & 上传)
    List<NetworkIF> networkIFs = hardware.getNetworkIFs();
    long currentRecv = 0;
    long currentSent = 0;

    for (NetworkIF net : networkIFs) {
      net.updateAttributes(); // 关键：更新实时数据
      currentRecv += net.getBytesRecv();
      currentSent += net.getBytesSent();
    }

    long currentTime = System.currentTimeMillis();
    double timeDiffSec = (currentTime - lastTime) / 1000.0;

    if (timeDiffSec > 0 && lastTime != 0) {
      // 计算每秒 KB 数 (1024)
      data.downSpeed = ((currentRecv - lastRecv) / 1024.0) / timeDiffSec;
      data.upSpeed = ((currentSent - lastSent) / 1024.0) / timeDiffSec;
    }

    lastRecv = currentRecv;
    lastSent = currentSent;
    lastTime = currentTime;

    // 5. 磁盘统计 (所有驱动器平均占用率)
    List<OSFileStore> fileStores = systemInfo.getOperatingSystem().getFileSystem().getFileStores();
    long totalDiskSpace = 0;
    long usableDiskSpace = 0;

    for (OSFileStore fs : fileStores) {
      totalDiskSpace += fs.getTotalSpace();
      usableDiskSpace += fs.getUsableSpace();
    }

    if (totalDiskSpace > 0) {
      data.diskUsage = (double) (totalDiskSpace - usableDiskSpace) / totalDiskSpace * 100;
    }

    return data;
  }
}
