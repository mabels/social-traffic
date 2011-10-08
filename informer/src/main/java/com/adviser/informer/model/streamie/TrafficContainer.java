package com.adviser.informer.model.streamie;

import lombok.Data;

@Data
public class TrafficContainer {
 
  
  private History traffic = new History();

 public void add(String ip, long timeStamp, long inAmount, long outAmount) {
  traffic.add(timeStamp, inAmount, outAmount);
 }
  /*
  final Iterator<Client> clients = getClients().iterator();
  while (clients.hasNext()) {
    final Client client = clients.next();
    if (client.getIpv4().equals(ip)) {
      client.getHistory().add(timeStamp, inAmount, outAmount);
      break;
    }
  }
  /*
  inTotal += inAmount;
  outTotal += outAmount;
  */
}
