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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import com.fasterxml.jackson.annotation.JsonFormat.Value;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import eu.hansolo.enzo.notification.Notification.Notifier;


public class Main extends Application {
  public static int maxNumValue = 0;

  @Override
  public void start(Stage primaryStage) {
    createMainWindow(primaryStage);
    // setUpTray();
  }

  public static void createMainWindow(Stage stage) {
    Notifier.INSTANCE.notifyWarning("Tああああhis is a warning", "Info-Mあああessage");
    Notifier.INSTANCE.notifyInfo("test", "");
    WebView view = new WebView();
    // view.set

    WebEngine engine = view.getEngine();
    engine.load("https://idobata.io/users/sign_in");

    stage.setScene(new Scene(view, 1024, 768));
    stage.show();
    Platform.setImplicitExit(false);
    setupTray(stage);
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
              CookieHandler cookieManager = CookieHandler.getDefault();
              try {
                Map<String, List<String>> map =
                    cookieManager.get(new URI("https://idobata.io/"),
                        new HashMap<String, List<String>>());
                // Content returnContent =
                // Request.Get("https://idobata.io/api/messages/")//.setHeader("X-API-Token",
                // "992ce37553d99d131bd55384ae9f9300")
                // .execute().returnContent();

                String allCookieString = map.get("Cookie").get(0);
                String[] cookieStrings = allCookieString.split(";");

                CookieStore cookieStore = new BasicCookieStore();
                for (String cookieString : cookieStrings) {
                  String trimedCookieString = cookieString.trim();
                  String[] cookieSetStrings = trimedCookieString.split("=");

                  BasicClientCookie cookie =
                      new BasicClientCookie(cookieSetStrings[0], cookieSetStrings[1]);
                  cookie.setDomain("idobata.io");
                  cookie.setPath("/");
                  cookieStore.addCookie(cookie);
                }
                // System.out
                HttpGet get = new HttpGet("https://idobata.io/api/messages/");
                DefaultHttpClient client = new DefaultHttpClient();
                client.setCookieStore(cookieStore);

                // get the cookies
                HttpResponse response = client.execute(get);
                HttpEntity entity = response.getEntity();

                BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(entity.getContent()));

                String line;
                String body = "";
                while ((line = bufferedReader.readLine()) != null) {
                  body += line;
                }

                ObjectMapper objectMapper = new ObjectMapper();
                ObjectReader objectReader =
                    objectMapper.reader().withRootName("messages")
                        .withType(new TypeReference<ArrayList<IdobataMessage>>() {});

                ArrayList<IdobataMessage> idobataMessages = new ArrayList<IdobataMessage>() {};
                try {
                  idobataMessages = objectReader.readValue(body);
                  System.out.println("aaasd");
                } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }

                for (IdobataMessage idobataMessage : idobataMessages) {
                  int value = idobataMessage.getId();
                  if (maxNumValue == 0) {
                    maxNumValue = value;
                    break;
                  }

                  if (value > maxNumValue) {
                    String message = idobataMessage.getBody();
                    message.replaceAll("<.+?>", "");
                    Notifier.INSTANCE.notifyInfo("", message);
                    maxNumValue = value;
                  }
                }

              } catch (IOException | URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
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
