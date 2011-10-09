package com.adviser.informer.model.traffic;

import java.io.Serializable;

import lombok.Data;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.deser.StdDeserializer.IntegerDeserializer;

@Data
public class IpTuple implements Serializable {
  private static final long serialVersionUID = -7389405833651561486L;

  private String srcIP;
  private String dstIP;
  private String prot;
  @JsonDeserialize(using = IntegerDeserializer.class)
  private Integer srcPort;
  @JsonDeserialize(using = IntegerDeserializer.class)
  private Integer dstPort;
  @JsonDeserialize(using = IntegerDeserializer.class)
  private Integer octets;
  @JsonDeserialize(using = IntegerDeserializer.class)
  private Integer packets;

}
