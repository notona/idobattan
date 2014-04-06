package idobattan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;


@JsonIgnoreProperties(ignoreUnknown=true)
final public class IdobataMessage {
  private String body;
  
  @JsonProperty("created_at")
  private String createdAt;
  
  private int id;
  
  @JsonProperty("room_id")
  private int roomId;
  
  @JsonProperty("sender_icon_url")
  private String senderIconUrl;
  
  private int senderId;
  
  @JsonProperty("sender_name")
  private String senderName;
  
  @JsonProperty("sender_type")
  private String senderType;
  
  public void setBody(String body) {
    this.body = body;
  }
  
  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public int getId() {
    return this.id;
  }
  
  public void setRoomId(int id) {
    this.roomId = id;
  }
  
  public void setSenderIconUrl(String url) {
    this.senderIconUrl = url;
  }
  
  public void setSenderId(int senderId) {
    this.senderId = senderId;
  }
  
  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }
  
  public void setSenderType(String senderType) {
    this.senderType = senderType;
  }
  
  public String getBody() {
    return this.body;
  }
}
