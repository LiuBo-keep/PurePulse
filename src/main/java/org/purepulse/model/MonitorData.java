package org.purepulse.model;

/**
 * @author aidan.liu
 * @version 1.0
 * @since 2026/3/10 14:48
 */
public class MonitorData {

  public double cpuUsage;
  public double cpuTemp;

  public long memUsed;
  public long memTotal;

  public double upSpeed;
  public double downSpeed;
  public double diskUsage;

  public MonitorData() {
  }

  public double getCpuUsage() {
    return cpuUsage;
  }

  public void setCpuUsage(double cpuUsage) {
    this.cpuUsage = cpuUsage;
  }

  public double getCpuTemp() {
    return cpuTemp;
  }

  public void setCpuTemp(double cpuTemp) {
    this.cpuTemp = cpuTemp;
  }

  public long getMemUsed() {
    return memUsed;
  }

  public void setMemUsed(long memUsed) {
    this.memUsed = memUsed;
  }

  public long getMemTotal() {
    return memTotal;
  }

  public void setMemTotal(long memTotal) {
    this.memTotal = memTotal;
  }

  public double getUpSpeed() {
    return upSpeed;
  }

  public void setUpSpeed(double upSpeed) {
    this.upSpeed = upSpeed;
  }

  public double getDownSpeed() {
    return downSpeed;
  }

  public void setDownSpeed(double downSpeed) {
    this.downSpeed = downSpeed;
  }

  public double getDiskUsage() {
    return diskUsage;
  }

  public void setDiskUsage(double diskUsage) {
    this.diskUsage = diskUsage;
  }
}