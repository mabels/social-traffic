package com.adviser.informer.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

public class ISO8601DateDeserializer extends JsonDeserializer<Object> {
  private final DateFormat formatter = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss");;
  // 2011-09-29T12:22:49.974Z
  public Date str2Date(String input) {
    
    try {
      /*
      if (input.endsWith("Z")) {
       
        input = input.substring(0, input.length() - 1) + "GMT-00:00";
      } else {
        int inset = 6;

        String s0 = input.substring(0, input.length() - inset);
        String s1 = input.substring(input.length() - inset, input.length());

        input = s0 + "GMT" + s1;
      }
      */
      return formatter.parse(input);
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot parse date:" + input, e);
    }
  }

  public Object deserialize(JsonParser jp, DeserializationContext ctxt) {
    try {
      return str2Date(jp.getText());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}