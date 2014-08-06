package idobattan;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

  public static void loadConfig(String configFilePath) throws IOException {
    try (InputStream is = new FileInputStream(configFilePath)) {
      properties.loadFromXML(is);
    }
  }

  public static void saveConfig() throws IOException {
    try (OutputStream os = new FileOutputStream("config2.xml")) {
      properties.storeToXML(os, "");
    }
  }

  public static String getProperty(String key) {
    return properties.getProperty(key);
  }

  public static void setProperty(String key, String value) {
    properties.setProperty(key, value);
  }
}
