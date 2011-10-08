package com.adviser.informer.model.streamie;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class StreamieLocation implements Serializable {

  private static final long serialVersionUID = -8987541694732648552L;

  private final String screenName;
  private final Details details;

  private final List<ClientLocation> clients;

  public StreamieLocation(Streamie streamie) {
    screenName = streamie.getTwitter().getScreenName();
    details = streamie.getTwitter().getDatails();
    clients = new LinkedList<ClientLocation>();
    Iterator<Client> i = streamie.getClients().iterator();
    while (i.hasNext()) {
      final Client cl = i.next();
      clients.add(new ClientLocation(cl));
    }
    /* MOCK */
    /* 
    final LinkedList<String> aps = new LinkedList<String>();
    aps.add("VAP1");
    aps.add("VAP2");
    aps.add("VAP3");
    aps.add("VAP4");
    aps.add("VAP5");
    aps.add("VAP6");
    aps.add("VAP7");
    aps.add("VAP8");
    aps.add("VAP9");
    aps.add("VAPA");
    aps.add("VAPB");
    aps.add("VAPC");
    aps.add("VAPD");
    aps.add("VAPE");
    aps.add("VAPF");
    aps.add("VAPG");
    aps.add("VAPH");
    aps.add("VAPI");
    aps.add("VAPJ");
    final Iterator<Client> i = clients.iterator();
    while (i.hasNext()) {
      final Client client = i.next();
      long cnt = Math.round(Math.random() * 9) + 1;
      final List<String> tmp = new LinkedList<String>();
      while (--cnt >= 0) {
        int idx = (int)Math.round(Math.random() * aps.size());
        if (idx >= aps.size()) {
          ++cnt;
          continue;
        }
        tmp.add(aps.get(idx));
      }
      client.setAccessPoints(tmp);
    }
    */
  }
}
