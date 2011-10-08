package com.adviser.informer.model.streamie;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.ektorp.support.CouchDbDocument;

@Data
@EqualsAndHashCode(callSuper=false)
public class Streamie extends CouchDbDocument /* implements Total */ {

  
  private static final long serialVersionUID = 978550300085359507L;

  private Twitter twitter;

  private List<Client> clients;
  
  private Completed completed;

}
