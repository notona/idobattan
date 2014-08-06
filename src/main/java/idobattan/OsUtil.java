package idobattan;

public class OsUtil {

  public static boolean isWin() {
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("win")) {
      return true;
    }

    return false;
  }

  public static boolean isMac() {
    String os = System.getProperty("os.name").toLowerCase();
    if (os.contains("mac")) {
      return true;
    }

    return false;
  }
}
