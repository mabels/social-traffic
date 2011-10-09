package com.adviser.informer.model.streamie;

import lombok.Data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown=true/*,value={"profile_background_image_url","profile_image_url_https", "profile_image_url", "statuses_count", "friends_count", "followers_count"}*/)
public class Details {
 
  //@JsonProperty("profile_background_image_url")
  private String profile_background_image_url;
  //@JsonProperty("profile_image_url_https")
  private String profile_image_url_https;
  //@JsonProperty("profile_image_url")
  private String profile_image_url;
  
  private String location;
  private String name;
  private String url;
  private String description;
  //@JsonProperty("statuses_count")
  private String statuses_count;
  //@JsonProperty("friends_count")
  private String friends_count;
  //@JsonProperty("followers_count")
  private Integer followers_count;
 }
