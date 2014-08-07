package idobattan;


import intellij.Foundation;
import intellij.ID;

import static intellij.Foundation.invoke;
import static intellij.Foundation.nsString;

public class NativeNotification {
  public static void notfify(String title, String message) {
    if (OsUtil.isMac()) {
      final ID notification = invoke(Foundation.getObjcClass("NSUserNotification"), "new");
      invoke(notification, "setTitle:", nsString(title));
      invoke(notification, "setInformativeText:", nsString(message));
      final ID center = invoke(Foundation.getObjcClass("NSUserNotificationCenter"), "defaultUserNotificationCenter");
      invoke(center, "deliverNotification:", notification);
    }
  }
}
