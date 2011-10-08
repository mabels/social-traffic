package com.adviser.informer.model.streamie;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@JsonIgnoreProperties(ignoreUnknown=true/*,value={"created_at"}*/)
public class Client implements Serializable {

  private static final long serialVersionUID = 743055082362827935L;

  private String ipv4;
  private String hwaddr;
  private String useragent;
  
  private List<String> accessPoints;
  public List<String> getAccessPoints() {
    if (accessPoints == null) {
      accessPoints = new LinkedList<String>();
    }
    return accessPoints;
  }
  //@JsonDeserialize(using = DateDeserializer.class)
  //@JsonDeserialize(using = ISO8601DateDeserializer.class)
  @JsonProperty("created_at")
  private Date createdAt;
}
