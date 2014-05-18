package idobattan;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.*;
import java.util.List;


public class Main extends Application {
  private static Logger logger = LoggerFactory.getLogger(Main.class);

  final Timer timer = new Timer();

  @Override
  public void start(Stage primaryStage) {
    // アプリケーションのアイコンを設定
    primaryStage.getIcons().add(
        new javafx.scene.image.Image(this.getClass().getResourceAsStream("/idobattan_icon.png")));

    createMainWindow(primaryStage);
    // setUpTray();
  }

  @FXML
  private WebView webView;
  @FXML
  private WebView timelineWebView;

  public void createMainWindow(Stage stage) {


    AnchorPane root = null;
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/front.fxml"));
      fxmlLoader.setController(this);

      root = fxmlLoader.load();
    } catch (Exception e) {
      logger.error("error", e);
    }
    Scene scene = new Scene(root);
    stage.setScene(scene);

    
    stage.show();
    Platform.setImplicitExit(false);
    setupTray(stage);

    WebEngine engine = webView.getEngine();
    setDefaultBrowser(engine);

    try {
      // http://stackoverflow.com/questions/14385233/setting-a-cookie-using-javafxs-webengine-webview
      String cookieString = FileUtils.readFileToString(new File("idobata_session.txt"));

      LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
      String[] cookieStrings = cookieString.split(";");
      headers.put("Set-Cookie", Arrays.asList(cookieStrings[0]));

      CookieHandler default1 = CookieHandler.getDefault();
      CookieHandler.getDefault().put(new URI("https://idobata.io"), headers);

    } catch (Exception e) {
      logger.error("error", e);
    }


    engine.load("https://idobata.io/users/sign_in");

    WebEngine engine2 = timelineWebView.getEngine();
    setDefaultBrowser(engine2);
    engine2.load("https://idobata.io/#/timeline");


    timer.scheduleAtFixedRate(new NotificationTimerTask(), 0, 10000); // 10sec
  }

  public void setDefaultBrowser(final WebEngine webEngine) {
    webEngine.locationProperty().addListener(new InvalidationListener() {
      @Override
      public void invalidated(javafx.beans.Observable observable) {
        ObservableValue o = (ObservableValue)observable;
        String nextLocation = (String)o.getValue();
        if (!nextLocation.contains("idobata.io")) {
          Platform.runLater(() -> {
            webEngine.getLoadWorker().cancel();
            getHostServices().showDocument(nextLocation);
          });
        }
      } 
    });
  }

  public void setupTray(final Stage stage) {
    if (!SystemTray.isSupported()) {
      return;
    }

    SystemTray tray = SystemTray.getSystemTray();
    BufferedImage image3 = null;
    try {
      image3 = ImageIO.read(this.getClass().getResource("/idobattan_icon.png"));
    } catch (IOException e) {
      logger.error("error", e);
    }

    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent arg0) {
        Platform.runLater(() -> {
          stage.hide();
        });
      }
    });

    ActionListener menuActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        switch (command) {
          case "なんか":
            logger.debug("nanka called");
            break;
          case "終了":
            try {
              CookieHandler cookieManager = CookieHandler.getDefault();
              Map<String, List<String>> map =
                  cookieManager.get(new URI("https://idobata.io/"),
                      new HashMap<String, List<String>>());

              String allCookieString = map.get("Cookie").get(0);
              FileUtils.writeStringToFile(new File("idobata_session.txt"), allCookieString);
            } catch (Exception e) {
              logger.error("error", e);
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
    icon2.setImageAutoSize(true);
    icon2.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
          Platform.runLater(() -> {
            stage.setIconified(false);
            stage.show();
          });
        }
      }
    });
    try {
      tray.add(icon2);
    } catch (AWTException e) {
      logger.error("error", e);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
