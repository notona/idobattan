package idobattan;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.sun.webkit.network.CookieManager;

import eu.hansolo.enzo.notification.Notification.Notifier;


public class Main extends Application {
  
  final Timer timer = new Timer();

  @Override
  public void start(Stage primaryStage) {
    // 空のウインドウが発生するのを防止する
    Notifier.setNotificationOwner(primaryStage);
    Notifier.setWidth(300.0);
    //Notifier.s
    //Notifier.

    createMainWindow(primaryStage);
    // setUpTray();
  }
  
  @FXML private WebView webView;
  @FXML private WebView timelineWebView;

  public void createMainWindow(Stage stage) {
    
    
    AnchorPane root = null;
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
          "front.fxml"));
      //fxmlLoader.setRoot(this);
      fxmlLoader.setController(this);
      
      root = fxmlLoader.load();
      System.out.println("tt");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      System.out.println(e.getMessage());
      System.exit(1);
    }
    Scene scene = new Scene(root);
    stage.setScene(scene);

    stage.show();
    Platform.setImplicitExit(false);
    setupTray(stage);
    
    WebEngine engine = webView.getEngine();
    
    try {
      // http://stackoverflow.com/questions/14385233/setting-a-cookie-using-javafxs-webengine-webview
      String cookieString = FileUtils.readFileToString(new File("idobata_session.txt"));

      Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
      String[] cookieStrings = cookieString.split(";");
      headers.put("Set-Cookie", Arrays.asList(cookieStrings[0]));

      CookieHandler default1 = CookieHandler.getDefault();
      CookieHandler.getDefault().put(new URI("https://idobata.io"), headers);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    
    engine.load("https://idobata.io/users/sign_in");

    WebEngine engine2 = timelineWebView.getEngine();
    engine2.load("https://idobata.io/#/timeline");
    
    
    timer.scheduleAtFixedRate(new NotificationThread(), 0, 2500);
  }

  private static void hide(final Stage stage) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        if (SystemTray.isSupported()) {
          stage.hide();
        } else {
          System.exit(0);
        }
      }
    });
  }

  public static void setupTray(final Stage stage) {
    SystemTray tray = SystemTray.getSystemTray();
    BufferedImage image3 = null;
    try {
      image3 = ImageIO.read(new File("idobattan_icon.png"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

      @Override
      public void handle(WindowEvent arg0) {
        // TODO Auto-generated method stub
        hide(stage);
      }
    });

    ActionListener menuActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        switch (command) {
          case "なんか":
            System.out.println("ss");
            break;
          case "終了":
            try {
              CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();

              Map<String, List<String>> map =
                  cookieManager.get(new URI("https://idobata.io/"),
                      new HashMap<String, List<String>>());

              String allCookieString = map.get("Cookie").get(0);
              FileUtils.writeStringToFile(new File("idobata_session.txt"), allCookieString);
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            
            System.exit(0);
            break;
        }

      }
    };

    PopupMenu menu = new PopupMenu();
    menu.add(new MenuItem("なんか")).addActionListener(menuActionListener);
    menu.addSeparator();
    menu.add(new MenuItem("終了")).addActionListener(menuActionListener);

    final TrayIcon icon2 = new TrayIcon(image3, "idobattan", menu);
    icon2.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              NotificationThread.notifiy();
              stage.show();
            }
            });
        }
      }
    });
    try {
      tray.add(icon2);
    } catch (AWTException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    launch(args);
  }


}