package com.adviser.informer.model.streamie;

import lombok.Data;
import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class Twitter implements Serializable {
  private static final long serialVersionUID = -6160452491334563871L;

  @JsonProperty("user_id")
  private String UserId;
  @JsonProperty("screen_name")
  private String screenName;
  private int statusCode;
  private Details details;
  public Details getDatails() {
    if (details == null) {
      details = new Details();
    }
    return details;
  }
  private String oauth;

}
