package com.adviser.informer.model.streamie;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;

public class AggregatedHistory implements Serializable{
  private static final long serialVersionUID = -3072716493928195083L;
  @Getter
  private final LinkedList<long[]> shortTerm;
  @Getter
  private final LinkedList<long[]> longTerm;
  public AggregatedHistory(History history) {
    longTerm = aggregate(history.getTraffic(), 10*60, 24*6*2);
    shortTerm = aggregate(history.getTraffic(), 60, 2*60);    
  }
  private LinkedList<long[]> aggregate(LinkedList<Tuple> in, long slot, long deep) {   
    final Map<Long, Tuple> tmp = new TreeMap<Long, Tuple>(); 
    Iterator<Tuple> i = in.iterator();
    while(i.hasNext()) {
      final Tuple newData = i.next();   
      final Long timestamp = (newData.getTimestamp()/slot)*slot;
      if (!(1000000000L < timestamp && timestamp < 2000000000L)) {
        continue;
      }
      //newData.setTimestamp(timestamp);
      Tuple tuple = tmp.get(timestamp);
      if (tuple == null) {
//System.out.println("t="+timestamp+":"+slot+":"+newData.getTimestamp()+":"+(newData.getTimestamp()/(slot)));
        tuple = new Tuple(timestamp);
        tmp.put(timestamp, tuple);
      }
      tuple.add(newData);
    }
    
    LinkedList<long[]> ret = new LinkedList<long[]>();
    i = tmp.values().iterator();
    
    while (--deep >= 0 && i.hasNext()) {
      ret.add(i.next().asArray());
    }
    return ret;
  }
  
}
