package idobattan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Config {

  private static Properties properties = new Properties();

  // use factory method to create instance
  private void Config() {
  }

  public static void loadConfig() throws IOException {
    String savePath = buildSaveDir() + "/config.xml";

    try (InputStream is = new FileInputStream(savePath)) {
      properties.loadFromXML(is);
    }
  }

  public static void saveConfig() throws IOException {
    String saveDir = buildSaveDir();
    (new File(saveDir)).mkdirs();

    String savePath = saveDir + "/config.xml";
    try (OutputStream os = new FileOutputStream(savePath)) {
      properties.storeToXML(os, "");
    }
  }

  private static String buildSaveDir() {
    String saveDir = null;
    if (OsUtil.isWin()) {
      // ex: "c:/users/user-name/Application Data"
      saveDir = System.getenv("AppData");
    } else if (OsUtil.isMac()) {
      // ex: "/Users/user-name/Library/Application Support"
      saveDir = System.getProperty("user.home") + "/Library/Application Support";
    } else {
      // ex: "/home/user-name"
      saveDir = System.getProperty("user.home");
    }
    return saveDir + "/Idobattan";
  }

  public static String getProperty(String key) {
    return properties.getProperty(key);
  }

  public static void setProperty(String key, String value) {
    properties.setProperty(key, value);
  }
}
