package org.purepulse;

import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.purepulse.model.MonitorData;

/**
 * PurePulse Desktop Monitor Application.
 *
 * <p>
 * This class represents the main entry point of the PurePulse desktop
 * monitoring application built using JavaFX.
 * </p>
 *
 * <p>
 * PurePulse is a lightweight floating system monitoring widget that
 * continuously displays real-time system statistics in a compact UI.
 * The application is designed to stay on top of the desktop and
 * provide quick insight into system performance without opening
 * heavyweight monitoring tools.
 * </p>
 *
 * <h2>Displayed Metrics</h2>
 * <ul>
 *   <li>CPU usage percentage</li>
 *   <li>Memory usage (used / total)</li>
 *   <li>Network upload and download speed</li>
 *   <li>Disk usage percentage</li>
 * </ul>
 *
 * <h2>Application Characteristics</h2>
 * <ul>
 *   <li>Always-on-top floating widget</li>
 *   <li>Minimalist UI design</li>
 *   <li>Smooth animated progress bars</li>
 *   <li>System tray integration</li>
 *   <li>Draggable window</li>
 *   <li>Right-click to hide window</li>
 * </ul>
 *
 * <p>
 * Monitoring data is periodically retrieved from {@link MonitorService},
 * then pushed to the UI using the JavaFX application thread via
 * {@link Platform#runLater(Runnable)}.
 * </p>
 *
 * <p>
 * The UI is intentionally compact and optimized for continuous background
 * monitoring with minimal resource usage.
 * </p>
 *
 * @author aidan.liu
 * @version 1.0
 * @since 2026-03-10
 */
public class PurePulseApp extends Application {

  /**
   * Monospaced font used for displaying numeric metrics.
   * Improves readability for values that frequently update.
   */
  private static final String FONT_MONO = "Consolas, 'Monospace'";

  /**
   * Semi-transparent background color of the widget container.
   */
  private static final String COLOR_BG = "rgba(255, 255, 255, 0.95)";
  /**
   * Primary text color used for general UI labels.
   */
  private static final String COLOR_TEXT_MAIN = "#333333";

  /**
   * Accent color definitions used to visually distinguish metrics.
   */
  private static final String COLOR_CYAN = "#0091EA";
  private static final String COLOR_BLUE = "#2979FF";
  private static final String COLOR_RED = "#D32F2F";
  private static final String COLOR_GREEN = "#2E7D32";
  private static final String COLOR_PURPLE = "#7B1FA2";
  private static final String COLOR_PINK = "#C2185B";

  /**
   * Threshold values used for warning indicators.
   * When system metrics exceed these thresholds, the UI may change color
   * to alert the user of high resource usage.
   */
  private static final double CPU_THRESHOLD = 80.0;
  private static final double MEM_THRESHOLD = 0.8;
  private static final double DISK_THRESHOLD = 0.9;

  /**
   * Service responsible for collecting system performance data.
   */
  private final MonitorService service = new MonitorService();

  /**
   * Labels used to display current metric values.
   */
  private final Label cpuLabel = new Label();
  private final Label memLabel = new Label();
  private final Label netLabel = new Label();
  private final Label diskLabel = new Label();

  /**
   * Progress bars representing resource utilization visually.
   */
  private final ProgressBar cpuBar = new ProgressBar(0);
  private final ProgressBar memBar = new ProgressBar(0);
  private final ProgressBar diskBar = new ProgressBar(0);

  /**
   * Application startup entry point invoked by the JavaFX runtime.
   *
   * <p>
   * Initializes the main layout, configures the window behavior,
   * enables system tray integration, and starts the monitoring thread.
   * </p>
   *
   * @param stage primary application window
   */
  @Override
  public void start(Stage stage) {
    Platform.setImplicitExit(false);
    VBox root = createMainLayout(stage);
    configureWindow(stage, root);
    TrayManager.setup(stage);
    startMonitorTask();
  }

  /**
   * Creates and initializes the main UI layout.
   *
   * <p>
   * The layout consists of:
   * </p>
   * <ul>
   *   <li>Custom title bar</li>
   *   <li>Network statistics</li>
   *   <li>CPU usage and progress bar</li>
   *   <li>Memory usage and progress bar</li>
   *   <li>Disk usage and progress bar</li>
   * </ul>
   *
   * @param stage application window
   * @return root VBox layout
   */
  private VBox createMainLayout(Stage stage) {
    // 1. 标题栏 + 最小化按钮
    Label title = new Label("pure pulse");
    title.setStyle("-fx-text-fill: " + COLOR_TEXT_MAIN + "; -fx-font-size: 12px; -fx-font-weight: 900;");

    // 最小化符号
    Button btnMin = new Button("—");
    btnMin.setStyle(
        "-fx-background-color: transparent; -fx-text-fill: #888; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 5 0 5;");
    btnMin.setOnMouseEntered(
        e -> btnMin.setStyle("-fx-background-color: #eee; -fx-text-fill: #333; -fx-padding: 0 5 0 5;"));
    btnMin.setOnMouseExited(
        e -> btnMin.setStyle("-fx-background-color: transparent; -fx-text-fill: #888; -fx-padding: 0 5 0 5;"));
    // 最小化逻辑
    btnMin.setOnAction(e -> stage.hide());

    HBox titleBar = new HBox(title);
    HBox spacer = new HBox();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    titleBar.getChildren().addAll(spacer, btnMin);
    titleBar.setAlignment(Pos.CENTER_LEFT);

    // 2. 初始化标签
    initLabel(netLabel, COLOR_PURPLE);
    initLabel(cpuLabel, COLOR_CYAN);
    initLabel(memLabel, COLOR_GREEN);
    initLabel(diskLabel, COLOR_PINK);

    // 3. 初始化进度条
    initBar(cpuBar);
    initBar(memBar);
    initBar(diskBar);

    // 4. 指标行（CPU）
    HBox cpuLine = new HBox(cpuLabel);
    HBox lineSpacer = new HBox();
    HBox.setHgrow(lineSpacer, Priority.ALWAYS);

    // --- 组装根容器 ---
    VBox root = new VBox(10);
    root.setPadding(new Insets(15, 20, 20, 20));
    root.setStyle("-fx-background-color: " + COLOR_BG +
        "; -fx-background-radius: 16; -fx-border-color: #ddd; -fx-border-radius: 16; -fx-border-width: 1;");

    // 添加轻微阴影，增加高级感
    DropShadow shadow = new DropShadow();
    shadow.setColor(Color.rgb(0, 0, 0, 0.1));
    shadow.setRadius(10);
    root.setEffect(shadow);

    root.getChildren().addAll(titleBar, netLabel, cpuLine, cpuBar, memLabel, memBar, diskLabel, diskBar);

    return root;
  }

  /**
   * Applies standard styling to metric labels.
   *
   * @param l     label component
   * @param color text color used for the metric
   */
  private void initLabel(Label l, String color) {
    l.setStyle(
        String.format("-fx-font-family: %s; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: %s;", FONT_MONO,
            color));
  }

  /**
   * Configures a progress bar used for displaying resource usage.
   *
   * @param b progress bar instance
   */
  private void initBar(ProgressBar b) {
    b.setMaxWidth(Double.MAX_VALUE);
    b.setPrefHeight(12);
    b.setStyle("-fx-accent: " + COLOR_BLUE + ";");
  }

  /**
   * Configures the stage (window) properties.
   *
   * <p>
   * Window features include:
   * </p>
   * <ul>
   *   <li>Transparent stage style</li>
   *   <li>Always-on-top behavior</li>
   *   <li>Drag-to-move functionality</li>
   *   <li>Right-click to hide window</li>
   * </ul>
   *
   * @param stage main application stage
   * @param root  root layout node
   */
  private void configureWindow(Stage stage, VBox root) {
    Scene scene = new Scene(root, 260, 240);
    scene.setFill(Color.TRANSPARENT);
    // 进度条轨道改为浅灰色
    scene.getStylesheets().add("data:text/css," +
        ".progress-bar { -fx-background-color: #f0f0f0; -fx-background-radius: 5; } " +
        ".progress-bar .track { -fx-background-color: transparent; } " +
        ".progress-bar .bar { -fx-background-radius: 5; }");

    stage.initStyle(StageStyle.TRANSPARENT);
    stage.setScene(scene);
    stage.setAlwaysOnTop(true);

    DragUtil.enableDrag(stage, root);

    // 保留右键隐藏功能
    root.setOnMouseClicked(e -> {
      if (e.getButton() == MouseButton.SECONDARY) {
        stage.hide();
      }
    });
    stage.show();
  }

  /**
   * Starts a background monitoring loop.
   *
   * <p>
   * A daemon thread continuously collects system metrics from
   * {@link MonitorService} every two seconds and updates the UI.
   * </p>
   */
  private void startMonitorTask() {
    Thread t = new Thread(() -> {
      while (true) {
        try {
          MonitorData data = service.collect();
          Platform.runLater(() -> refreshUi(data));
          TimeUnit.SECONDS.sleep(2);
        } catch (Exception ignored) {
        }
      }
    });
    t.setDaemon(true);
    t.start();
  }

  /**
   * Refreshes the UI using the latest monitoring data.
   *
   * @param data latest system monitoring snapshot
   */
  private void refreshUi(MonitorData data) {
    netLabel.setText(String.format("NET ↓%s  ↑%s", formatNet(data.getDownSpeed()), formatNet(data.getUpSpeed())));

    cpuLabel.setText(String.format("CPU %.1f%%", data.getCpuUsage()));
    updateStatus(cpuLabel, cpuBar, data.getCpuUsage() >= CPU_THRESHOLD, COLOR_CYAN);

    double memRatio = (double) data.getMemUsed() / Math.max(1, data.getMemTotal());
    memLabel.setText(String.format("MEM %d / %d GB", data.getMemUsed(), data.getMemTotal()));
    updateStatus(memLabel, memBar, memRatio >= MEM_THRESHOLD, COLOR_GREEN);

    diskLabel.setText(String.format("DISK %.1f%%", data.getDiskUsage()));
    updateStatus(diskLabel, diskBar, (data.getDiskUsage() / 100.0) >= DISK_THRESHOLD, COLOR_PINK);

    smoothUpdate(cpuBar, data.getCpuUsage() / 100.0);
    smoothUpdate(memBar, memRatio);
    smoothUpdate(diskBar, data.getDiskUsage() / 100.0);
  }

  private void updateStatus(Label l, ProgressBar b, boolean isHigh, String normalColor) {
    String targetColor = isHigh ? COLOR_RED : normalColor;
    l.setStyle(
        String.format("-fx-font-family: %s; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: %s;", FONT_MONO,
            targetColor));
    b.setStyle("-fx-accent: " + (isHigh ? COLOR_RED : COLOR_BLUE) + ";");
  }

  /**
   * Formats network speed into a human-readable string.
   *
   * @param kbps network speed in kilobytes per second
   * @return formatted speed string
   */
  private String formatNet(double kbps) {
    if (kbps < 1024) {
      return String.format("%.0f KB/s", kbps);
    }
    return String.format("%.1f MB/s", kbps / 1024.0);
  }

  /**
   * Performs smooth animation when updating progress bar values.
   *
   * @param bar   progress bar
   * @param value new progress value (0.0 - 1.0)
   */
  private void smoothUpdate(ProgressBar bar, double value) {
    new Timeline(new KeyFrame(Duration.millis(400), new KeyValue(bar.progressProperty(), value))).play();
  }

  /**
   * Program entry point used when launching from command line.
   *
   * @param args runtime arguments
   */
  public static void main(String[] args) {
    launch(args);
  }
}