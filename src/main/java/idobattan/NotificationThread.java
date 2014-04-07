package idobattan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javafx.application.Platform;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.jsoup.Jsoup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import eu.hansolo.enzo.notification.Notification.Notifier;

public class NotificationThread extends TimerTask {
  public static int maxNumValue = 0;
  
  @Override
  public void run() {
    Platform.runLater(new Runnable() {
      public void run() {
        notifiy();
      }
  });
    
  }
  
  public static void notifiy() {
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

      if (maxNumValue == 0 && idobataMessages.size() != 0) {
        IdobataMessage idobataMessage = idobataMessages.get(idobataMessages.size() - 1);
        maxNumValue = idobataMessage.getId();
      } else {
        for (IdobataMessage idobataMessage : idobataMessages) {
          int value = idobataMessage.getId();

          if (value > maxNumValue) {
            String htmlMessage = idobataMessage.getBody();
            // String message = htmlMessage.replaceAll("^<div>(.+)</div>$", "$1");
            String message = Jsoup.parse(htmlMessage).text();
            Notifier.INSTANCE.notifyInfo(idobataMessage.getSenderName(), message);

            maxNumValue = value;
          }
        }
      }

    } catch (IOException | URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
