package com.adviser.informer.model;

import java.io.Serializable;

import lombok.Data;

import com.adviser.informer.model.streamie.StreamieContainer;

@Data
public class TopItem implements Serializable {
  private static final long serialVersionUID = -8410833657343477207L;
  private final String screenName;
  private final long inTotal;
  private final long outTotal;
  public TopItem(StreamieContainer sc) {
    screenName = sc.getStreamie().getTwitter().getScreenName();
    inTotal = sc.getTrafficContainer().getTraffic().getInTotal();
    outTotal = sc.getTrafficContainer().getTraffic().getOutTotal();
  }
}
