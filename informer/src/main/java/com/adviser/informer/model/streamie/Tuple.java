package com.adviser.informer.model.streamie;

import java.io.Serializable;

import lombok.Data;

@Data
public class Tuple implements Serializable {
  private static final long serialVersionUID = 2427119683268512282L;
  public Tuple(long _timestamp) {
    timestamp = _timestamp;
  }

  public void add(Tuple tuple) {
    inAmount += tuple.inAmount;
    outAmount += tuple.outAmount;
  }
  public long[] asArray() {
    long[] ret = new long[3];
    ret[0] = timestamp;
    ret[1] = inAmount;
    ret[2] = outAmount;
    return ret;
  }
  public final long timestamp;
  public long inAmount = 0;
  public long outAmount = 0;
}
