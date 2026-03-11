package org.purepulse.model;

/**
 * Data model representing the current system monitoring snapshot.
 *
 * <p>This class is used to store runtime hardware and system metrics
 * collected from the monitoring service. The data is typically gathered
 * from the system using libraries such as OSHI and then displayed
 * in the desktop monitoring panel.</p>
 *
 * <p>The metrics include:</p>
 * <ul>
 *     <li>CPU usage percentage</li>
 *     <li>Memory usage statistics</li>
 *     <li>Network upload/download speed</li>
 *     <li>Disk usage percentage</li>
 * </ul>
 *
 * <p>Each instance represents a single sampling result of the system state.</p>
 *
 * @author aidan.liu
 * @since 2026-03-10
 */
public class MonitorData {

  /**
   * Current CPU usage percentage.
   * <p>Range: 0.0 - 100.0</p>
   */
  private double cpuUsage;

  /**
   * Used system memory in bytes.
   */
  private long memUsed;

  /**
   * Total system memory in bytes.
   */
  private long memTotal;

  /**
   * Current network upload speed.
   * <p>Unit: bytes per second (B/s)</p>
   */
  private double upSpeed;

  /**
   * Current network download speed.
   * <p>Unit: bytes per second (B/s)</p>
   */
  private double downSpeed;

  /**
   * Disk usage percentage.
   * <p>Range: 0.0 - 100.0</p>
   */
  private double diskUsage;

  /**
   * Default constructor.
   */
  public MonitorData() {
  }

  /**
   * Returns the current CPU usage percentage.
   *
   * @return CPU usage (0.0 - 100.0)
   */
  public double getCpuUsage() {
    return cpuUsage;
  }

  /**
   * Sets the CPU usage percentage.
   *
   * @param cpuUsage CPU usage value
   */
  public void setCpuUsage(double cpuUsage) {
    this.cpuUsage = cpuUsage;
  }

  /**
   * Returns the used memory.
   *
   * @return used memory in bytes
   */
  public long getMemUsed() {
    return memUsed;
  }

  /**
   * Sets the used memory.
   *
   * @param memUsed used memory in bytes
   */
  public void setMemUsed(long memUsed) {
    this.memUsed = memUsed;
  }

  /**
   * Returns the total memory available on the system.
   *
   * @return total memory in bytes
   */
  public long getMemTotal() {
    return memTotal;
  }

  /**
   * Sets the total memory.
   *
   * @param memTotal total memory in bytes
   */
  public void setMemTotal(long memTotal) {
    this.memTotal = memTotal;
  }

  /**
   * Returns the network upload speed.
   *
   * @return upload speed in bytes per second
   */
  public double getUpSpeed() {
    return upSpeed;
  }

  /**
   * Sets the network upload speed.
   *
   * @param upSpeed upload speed in bytes per second
   */
  public void setUpSpeed(double upSpeed) {
    this.upSpeed = upSpeed;
  }

  /**
   * Returns the network download speed.
   *
   * @return download speed in bytes per second
   */
  public double getDownSpeed() {
    return downSpeed;
  }

  /**
   * Sets the network download speed.
   *
   * @param downSpeed download speed in bytes per second
   */
  public void setDownSpeed(double downSpeed) {
    this.downSpeed = downSpeed;
  }

  /**
   * Returns the disk usage percentage.
   *
   * @return disk usage percentage (0.0 - 100.0)
   */
  public double getDiskUsage() {
    return diskUsage;
  }

  /**
   * Sets the disk usage percentage.
   *
   * @param diskUsage disk usage percentage
   */
  public void setDiskUsage(double diskUsage) {
    this.diskUsage = diskUsage;
  }
}