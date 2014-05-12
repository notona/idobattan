package idobattan;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.controlsfx.control.Notifications;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.util.Duration;

public class NotificationTimerTask extends TimerTask {
  private static Logger logger = LoggerFactory.getLogger(NotificationTimerTask.class);
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
      
      HttpGet get = new HttpGet("https://idobata.io/api/messages/");
      HttpClient httpclient = HttpClientBuilder.create()
          .setUserAgent("idobattan")
          .setDefaultCookieStore(cookieStore)
          .build();
      HttpResponse response = httpclient.execute(get);

      // get the cookies
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
        logger.debug("idobata message received");
      } catch (IOException e) {
        logger.error("error", e);
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
            
            if (message.length() > 80) {
              message = StringUtils.abbreviate(message, 80);
            }
            
            Properties prop = new Properties();
            InputStream is;
            try {
              is = new FileInputStream("config.xml");
              prop.loadFromXML(is); // after this, closed automatically
            } catch (Exception e) {
              logger.error("error", e);
            }
            
            String keywordString = prop.getProperty("keywords");
            String[] keywords = keywordString.split(",");

            for (String keyword : keywords) {
              String trimedKeyword = keyword.trim();
              logger.debug(trimedKeyword);
              if (message.contains(trimedKeyword)) {
                Notifications.create()
                .title(idobataMessage.getSenderName())
                .text(message)
                .hideAfter(new Duration(4000.0))
                .position(Pos.TOP_RIGHT).owner(null)
                .show();
                break;
              }
            }

            maxNumValue = value;
          }
        }
      }

    } catch (IOException | URISyntaxException e) {
      logger.error("error", e);
    }
  }
}
