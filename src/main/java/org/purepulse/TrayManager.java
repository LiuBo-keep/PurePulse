package org.purepulse;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Manages the system tray integration for the PurePulse desktop application.
 *
 * <p>This utility class is responsible for creating and registering the system
 * tray icon, along with its context menu. The tray icon allows users to
 * control the application even when the main window is hidden.</p>
 *
 * <p>Main features provided by this class:</p>
 * <ul>
 *     <li>Adds an icon to the operating system system tray</li>
 *     <li>Provides a context menu with "Show" and "Exit" actions</li>
 *     <li>Allows restoring the application window</li>
 *     <li>Supports double-click behavior to reopen the window</li>
 * </ul>
 *
 * <p>This is commonly used for desktop utilities such as system monitors,
 * background tools, and floating widgets.</p>
 *
 * <p>Note: System tray support depends on the operating system and desktop
 * environment.</p>
 *
 * @author aidan.liu
 * @since 2026-03-10
 */
public class TrayManager {

  private static TrayIcon trayIcon;

  /**
   * Initializes the system tray icon and menu for the application.
   *
   * <p>If the system tray is not supported on the current platform,
   * the method simply returns without performing any action.</p>
   *
   * <p>The tray menu contains the following actions:</p>
   * <ul>
   *     <li><b>Show PurePulse</b> - Restores and brings the application window to the front</li>
   *     <li><b>Exit</b> - Terminates the application completely</li>
   * </ul>
   *
   * <p>Additionally, double-clicking the tray icon will restore the
   * application window.</p>
   *
   * @param stage the main JavaFX window of the application
   */
  public static void setup(Stage stage) {
    try {

      // Check whether the current system supports a system tray
      if (!SystemTray.isSupported()) {
        return;
      }

      SystemTray tray = SystemTray.getSystemTray();

      // Load tray icon image (recommended size: 16x16 PNG)
      java.awt.Image image = Toolkit.getDefaultToolkit()
          .createImage(TrayManager.class.getResource("/image/tray.png"));

      PopupMenu menu = new PopupMenu();

      MenuItem showItem = new MenuItem("Show PurePulse");
      MenuItem exitItem = new MenuItem("Exit");

      // Restore application window
      showItem.addActionListener(e -> Platform.runLater(() -> {
        stage.show();
        stage.setIconified(false);
        stage.toFront();
      }));

      // Exit application
      exitItem.addActionListener(e -> {
        System.exit(0);
      });

      menu.add(showItem);
      menu.addSeparator();
      menu.add(exitItem);

      if (trayIcon != null) {
        return;
      }

      // Create tray icon
      trayIcon = new TrayIcon(image, "PurePulse", menu);
      trayIcon.setImageAutoSize(true);

      // Double-click tray icon to restore window
      trayIcon.addActionListener(e -> Platform.runLater(() -> {
        stage.show();
        stage.setIconified(false);
      }));

      try {
        tray.add(trayIcon);
      } catch (AWTException e) {
        e.printStackTrace();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}