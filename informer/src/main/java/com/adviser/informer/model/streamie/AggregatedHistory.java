package com.adviser.informer.model.streamie;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lombok.Getter;

public final class AggregatedHistory implements Serializable{
  private static final long serialVersionUID = -3072716493928195083L;
  @Getter
  private final List<long[]> shortTerm;
  @Getter
  private final List<long[]> longTerm;
  private static final int MIN = 60;
  private static final int MIN10 = MIN * 10;
  /*
  private AggregatedHistory() {
    shortTerm = longTerm = null;
  }
  */
  public AggregatedHistory(History history) {
    longTerm = aggregate(history.getTraffic(), MIN10, 24*6*2);
    shortTerm = aggregate(history.getTraffic(), MIN, 2*MIN);    
  }
  private List<long[]> aggregate(List<Tuple> in, long slot, long deep) {   
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
    
    while ((--deep) >= 0 && i.hasNext()) {
      ret.add(i.next().asArray());
    }
    return ret;
  }
  
}
