package org.purepulse;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import javafx.application.Platform;
import javafx.stage.Stage;

public class TrayManager {
  public static void setup(Stage stage) {
    try {
      if (!SystemTray.isSupported()) {
        return;
      }

      SystemTray tray = SystemTray.getSystemTray();
      // 建议：给托盘找一个 16x16 的透明 png 图标，BufferedImage 默认为全黑
      java.awt.Image image = Toolkit.getDefaultToolkit().createImage(TrayManager.class.getResource("/image/tray.png"));

      PopupMenu menu = new PopupMenu();
      MenuItem showItem = new MenuItem("Show PurePulse");
      MenuItem exitItem = new MenuItem("Exit");

      showItem.addActionListener(e -> Platform.runLater(() -> {
        stage.show();
        stage.setIconified(false);
        stage.toFront();
      }));

      exitItem.addActionListener(e -> {
        System.exit(0); // 彻底退出
      });

      menu.add(showItem);
      menu.addSeparator();
      menu.add(exitItem);

      TrayIcon icon = new TrayIcon(image, "PurePulse", menu);
      icon.setImageAutoSize(true);
      // 双击图标恢复窗口
      icon.addActionListener(e -> Platform.runLater(() -> {
        stage.show();
        stage.setIconified(false);
      }));

      try {
        tray.add(icon);
      } catch (AWTException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
