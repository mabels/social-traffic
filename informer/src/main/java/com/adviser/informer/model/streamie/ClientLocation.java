package com.adviser.informer.model.streamie;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ClientLocation implements Serializable {
  private static final long serialVersionUID = -5207635030717987457L;
  private final String ipv4;
  private final String hwaddr;
  private final String useragent;
  
  private final String accessPoint;
  public ClientLocation(Client client) {
      ipv4 = client.getIpv4();
      hwaddr = client.getHwaddr();
      useragent = client.getUseragent();
      List<String> aps = client.getAccessPoints();
      if (aps.size() > 0) {
        accessPoint = aps.get(aps.size()-1);
      } else {
        accessPoint = null;
      }
  }
}
