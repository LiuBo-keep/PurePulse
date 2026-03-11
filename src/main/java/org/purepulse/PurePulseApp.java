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

public class PurePulseApp extends Application {

  // --- 浅色主题颜色定义 ---
  private static final String FONT_MONO = "Consolas, 'Monospace'";
  private static final String COLOR_BG = "rgba(255, 255, 255, 0.95)"; // 纯白半透明
  private static final String COLOR_TEXT_MAIN = "#333333";           // 主文字色

  private static final String COLOR_CYAN = "#0091EA";   // 深青色（适配白底）
  private static final String COLOR_BLUE = "#2979FF";   // 进度条色
  private static final String COLOR_RED = "#D32F2F";    // 警报红
  private static final String COLOR_ORANGE = "#E65100"; // 温度色
  private static final String COLOR_GREEN = "#2E7D32";  // 内存色
  private static final String COLOR_PURPLE = "#7B1FA2"; // 网络色
  private static final String COLOR_PINK = "#C2185B";   // 磁盘色

  // 阈值
  private static final double CPU_THRESHOLD = 80.0;
  private static final double MEM_THRESHOLD = 0.8;
  private static final double DISK_THRESHOLD = 0.9;
  private static final double TEMP_THRESHOLD = 80.0;

  // UI 组件
  private final MonitorService service = new MonitorService();
  private final Label cpuLabel = new Label();
  private final Label memLabel = new Label();
  private final Label netLabel = new Label();
  private final Label diskLabel = new Label();

  private final ProgressBar cpuBar = new ProgressBar(0);
  private final ProgressBar memBar = new ProgressBar(0);
  private final ProgressBar diskBar = new ProgressBar(0);

  @Override
  public void start(Stage stage) {
    Platform.setImplicitExit(false);
    VBox root = createMainLayout(stage);
    configureWindow(stage, root);
    TrayManager.setup(stage);
    startMonitorTask();
  }

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

  private void initLabel(Label l, String color) {
    l.setStyle(
        String.format("-fx-font-family: %s; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: %s;", FONT_MONO,
            color));
  }

  private void initBar(ProgressBar b) {
    b.setMaxWidth(Double.MAX_VALUE);
    b.setPrefHeight(12);
    b.setStyle("-fx-accent: " + COLOR_BLUE + ";");
  }

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

  private void refreshUi(MonitorData data) {
    netLabel.setText(String.format("NET ↓%s  ↑%s", formatNet(data.downSpeed), formatNet(data.upSpeed)));

    cpuLabel.setText(String.format("CPU %.1f%%", data.cpuUsage));
    updateStatus(cpuLabel, cpuBar, data.cpuUsage >= CPU_THRESHOLD, COLOR_CYAN);

    double memRatio = (double) data.memUsed / Math.max(1, data.memTotal);
    memLabel.setText(String.format("MEM %d / %d GB", data.memUsed, data.memTotal));
    updateStatus(memLabel, memBar, memRatio >= MEM_THRESHOLD, COLOR_GREEN);

    diskLabel.setText(String.format("DISK %.1f%%", data.diskUsage));
    updateStatus(diskLabel, diskBar, (data.diskUsage / 100.0) >= DISK_THRESHOLD, COLOR_PINK);

    smoothUpdate(cpuBar, data.cpuUsage / 100.0);
    smoothUpdate(memBar, memRatio);
    smoothUpdate(diskBar, data.diskUsage / 100.0);
  }

  private void updateStatus(Label l, ProgressBar b, boolean isHigh, String normalColor) {
    String targetColor = isHigh ? COLOR_RED : normalColor;
    l.setStyle(
        String.format("-fx-font-family: %s; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: %s;", FONT_MONO,
            targetColor));
    b.setStyle("-fx-accent: " + (isHigh ? COLOR_RED : COLOR_BLUE) + ";");
  }

  private String formatNet(double kbps) {
    if (kbps < 1024) {
      return String.format("%.0f KB/s", kbps);
    }
    return String.format("%.1f MB/s", kbps / 1024.0);
  }

  private void smoothUpdate(ProgressBar bar, double value) {
    new Timeline(new KeyFrame(Duration.millis(400), new KeyValue(bar.progressProperty(), value))).play();
  }

  public static void main(String[] args) {
    launch(args);
  }
}