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

import javax.imageio.ImageIO;

import eu.hansolo.enzo.notification.Notification.Notifier;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
        createMainWindow(primaryStage);
        //setUpTray();
	}
	
	public static void createMainWindow(Stage stage) {
		//Notifier.INSTANCE.notifyWarning("This is a warning", "Info-Message");
		WebView view = new WebView();
		
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
