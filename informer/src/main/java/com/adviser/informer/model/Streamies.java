package com.adviser.informer.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.Getter;

import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.impl.StdCouchDbConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adviser.informer.model.streamie.Client;
import com.adviser.informer.model.streamie.History;
import com.adviser.informer.model.streamie.Streamie;
import com.adviser.informer.model.streamie.StreamieContainer;
import com.adviser.informer.model.streamie.TrafficContainer;
import com.adviser.informer.model.traffic.IpTuple;
import com.adviser.informer.model.traffic.Traffic;

public final class Streamies extends Observable implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

  
  private final static long MSECINSEC = 1000L;
  private final static int SECPERMIN = 60;
  private final static int RETRYSEC = 5;
  public static final int INITIALIZE = 1;

  private class ByTraffic implements Runnable, Serializable {
    private static final long serialVersionUID = 4981832330424047439L;

    private final Map<String, StreamieContainer> ids;

    public ByTraffic(Map<String, StreamieContainer> ids) {
      this.ids = ids;
    }

    private PriorityQueue<StreamieContainer> traffic = null;

    public List<StreamieContainer> top(int count) {
      final List<StreamieContainer> ret = new LinkedList<StreamieContainer>();
      if (traffic == null) {
        return ret;
      }
      final Iterator<StreamieContainer> i = traffic.iterator();
      for (int j = 0; j < count && i.hasNext(); ++j) {
        ret.add(i.next());
      }
      return ret;
    }

    public void run() {
      int timer = RETRYSEC;
      while (true) {
        try {
          Thread.sleep(timer * MSECINSEC);
          if (timer < SECPERMIN) {
            timer += RETRYSEC;
          }
          LOGGER.info("Start TOP10");
          final PriorityQueue<StreamieContainer> tmp = new PriorityQueue<StreamieContainer>(
              ids.size(), new Comparator<StreamieContainer>() {

                public int compare(StreamieContainer arg0, StreamieContainer arg1) {
                  final long ret = getTotal(arg1.getTrafficContainer()) - getTotal(arg0.getTrafficContainer());
                  if (ret > 0) {
                    return 1;
                  } else if (ret < 0) {
                    return -1;
                  }
                  return 0;
                }

                public long getTotal(TrafficContainer tc) {
                  return tc.getTraffic().getInTotal()
                      + tc.getTraffic().getOutTotal();
                }
              });
          final Iterator<StreamieContainer> i = ids.values().iterator();
          while (i.hasNext()) {
            final StreamieContainer tc = i.next();
            tmp.add(tc);
          }
          LOGGER.debug("Done TOP10");
          traffic = tmp;
        } catch (InterruptedException e) {
          LOGGER.error("TOP10:", e);
        }

      }
    }
  }

  @Getter
  private final Map<String, StreamieContainer> byId;
  private final Map<String, StreamieContainer> byTwitter;
  private final Map<String, StreamieContainer> byIp;
  private final Map<String, StreamieContainer> byMac;
  @Getter
  private final ByTraffic byTraffic;
  @Getter
  private final History totalTraffic;

  private Streamies() {
    totalTraffic = new History();
    byId = new ConcurrentHashMap<String, StreamieContainer>();
    byTwitter = new ConcurrentHashMap<String, StreamieContainer>();
    byIp = new ConcurrentHashMap<String, StreamieContainer>();
    byMac = new ConcurrentHashMap<String, StreamieContainer>();
    byTraffic = new ByTraffic(byId);
    new Thread(byTraffic).start();
  }

  public StreamieContainer findById(String id) {
    return byId.get(id);
  }

  public StreamieContainer findByTwitter(String screename) {
    return byTwitter.get(screename);
  }

  private void remove(String id) {
    if (id == null) {
      return;
    }
    final StreamieContainer sc = byId.get(id);
    if (sc == null) {
      return;
    }
    final Streamie str = sc.getStreamie();
    byId.remove(str.getId());
    byTwitter.remove(str.getTwitter().getScreenName());
    final Iterator<Client> clients = str.getClients().iterator();
    while (clients.hasNext()) {
      final Client client = clients.next();
      byIp.remove(client.getIpv4());
      byMac.remove(client.getHwaddr());
    }
  }

  private void add(Streamie str) {
    remove(str.getId());
    final StreamieContainer sc = new StreamieContainer();
    sc.setStreamie(str);
    byId.put(str.getId(), sc);
    byTwitter.put(str.getTwitter().getScreenName(), sc);
    final Iterator<Client> clients = str.getClients().iterator();
    while (clients.hasNext()) {
      final Client client = clients.next();
      byIp.put(client.getIpv4(), sc);
      byMac.put(client.getHwaddr(), sc);
    }
  }

  private static Streamies my = null;

  public static Streamies init() {
    if (my == null) {
      my = new Streamies();
      new Thread(my).start();
    }
    return my;
  }

  private class Fetchers implements Runnable {

    private final BlockingQueue<DocumentChange> q;
    private final Completed c;

    public Fetchers(BlockingQueue<DocumentChange> q, Completed c) {
      this.q = q;
      this.c = c;
    }

    public void run() {
      final StdCouchDbConnector db = new StdCouchDbConnector("streamie",
          CouchDB.connection());
      try {
        while (true) {
          final DocumentChange dc = q.take();
          if (dc.isDeleted()) {
            remove(dc.getId());
          } else {
            final Streamie streamie = db.get(Streamie.class, dc.getId());
            add(streamie);
          }
          LOGGER.info("Streamie:" + dc.getId());
          c.done(dc.getSequence());
        }
      } catch (Exception e) {
        LOGGER.error("Streamie:", e);
      }

    }
  }

  public abstract class Completed {

    private long last = 0;

    public Completed(long last) {
      this.last = last;
    }

    public void done(long seq) {
      if (seq == last) {
        completed(seq);
      }
    }

    public abstract void completed(long last);

  }

  public void add(Traffic traffic) {
    final Iterator<IpTuple> tuples = traffic.getTuples().iterator();
    while (tuples.hasNext()) {
      final IpTuple tuple = tuples.next();
      long inAmount = 0;
      long outAmount = 0;
      String matchIp = tuple.getDstIP();
      StreamieContainer streamie = byIp.get(matchIp);
      if (streamie == null) {
        streamie = byTwitter.get(matchIp);
        if (streamie == null) {
          matchIp = tuple.getSrcIP();
          streamie = byIp.get(matchIp);
          if (streamie == null) {
            streamie = byTwitter.get(matchIp);
            if (streamie == null) {
              continue;
            } else {
              outAmount = tuple.getOctets();
            }
          } else {
            outAmount = tuple.getOctets();
          }
        } else {
          inAmount = tuple.getOctets();
        }
      } else {
        inAmount = tuple.getOctets();
      }
      final long timeStamp = traffic.getCreatedAt().getTime() / MSECINSEC;
      // System.out.println(timeStamp);
      totalTraffic.add(timeStamp, inAmount, outAmount);
      streamie.getTrafficContainer().add(matchIp, timeStamp, inAmount, outAmount);
    }
  }

  public List<StreamieContainer> top(int count) {
    return byTraffic.top(count);
  }

  public void run() {

    final BlockingQueue<DocumentChange> q = new LinkedBlockingQueue<DocumentChange>();
    final ChangesCommand cmd = new ChangesCommand.Builder().since(0).build();
    final StdCouchDbConnector db = new StdCouchDbConnector("streamie",
        CouchDB.connection());
    final List<DocumentChange> feed = db.changes(cmd);

    q.addAll(feed);
    LOGGER.info("Initial Read until len: {}:{}", feed, feed.get(feed.size() - 1).getSequence());

    final Streamies self = this;
    final Completed c = new Completed(feed.get(feed.size() - 1).getSequence()) {

      @Override
      public void completed(long last) {
        LOGGER.info("Initial Read completed until: {}", last);
        final ChangesCommand cmd = new ChangesCommand.Builder().since(last)
            .build();
        final ChangesFeed feed = db.changesFeed(cmd);
        self.setChanged();
        self.notifyObservers(Streamies.INITIALIZE);
        while (feed.isAlive()) {
          try {
            final DocumentChange change = feed.next();
            q.add(change);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }

      }
    };
    new Thread(new Fetchers(q, c)).start();
    new Thread(new Fetchers(q, c)).start();
  }
}
