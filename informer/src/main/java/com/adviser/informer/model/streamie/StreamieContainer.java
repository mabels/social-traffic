package com.adviser.informer.model.streamie;

import lombok.Data;

@Data
public class StreamieContainer {

  private Streamie streamie;
  private TrafficContainer trafficContainer = null;
  public TrafficContainer getTrafficContainer() {
    if (trafficContainer == null) {
      trafficContainer = new TrafficContainer();
    }
    return trafficContainer;
  }
}
