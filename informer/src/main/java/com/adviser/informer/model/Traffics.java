package com.adviser.informer.model;

import java.util.List;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.ektorp.changes.ChangesCommand;
import org.ektorp.changes.ChangesFeed;
import org.ektorp.changes.DocumentChange;
import org.ektorp.impl.StdCouchDbConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adviser.informer.model.Streamies.Completed;
import com.adviser.informer.model.traffic.Traffic;

public final class Traffics extends Observable implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Traffics.class);

  private Streamies streamies = null;

  private Traffics(Streamies streamies) {
    this.streamies = streamies;
  }

  public static Traffics init(Streamies streamies) {
    Traffics ret = new Traffics(streamies);
    new Thread(ret).start();
    return ret;
  }

  private class Fetchers implements Runnable {



    private BlockingQueue<DocumentChange> q;
    private Completed c;

    public Fetchers(BlockingQueue<DocumentChange> q, Completed c) {
      this.q = q;
      this.c = c;
    }

    public void run() {
      final StdCouchDbConnector db = new StdCouchDbConnector("traffic",
          CouchDB.connection());
      try {
        while (true) {
          final DocumentChange dc = q.take();
          // if (true) /* || dc.getId().compareTo("2011-05-02.133001") < 0) */ {
          if (dc.isDeleted()) {
            throw new RuntimeException("traffic could not deleted");
          } else {
            try {
              final Traffic traffic = db.get(Traffic.class, dc.getId());
              streamies.add(traffic);
            } catch (Exception e) {
              LOGGER.error("Traffics:ERROR: {}", dc.getId(), e);
            }
          }
          // }
          // System.out.println("Traffics:" + dc.getId());
          c.done(dc.getSequence());
        }
      } catch (Exception e) {
        LOGGER.error("Error:Traffic:", e);
      }

    }
  }

  public void run() {
    final BlockingQueue<DocumentChange> q = new LinkedBlockingQueue<DocumentChange>();
    final ChangesCommand cmd = new ChangesCommand.Builder().since(0).build();
    final StdCouchDbConnector db = new StdCouchDbConnector("traffic",
        CouchDB.connection());
    final List<DocumentChange> feed = db.changes(cmd);

    q.addAll(feed);
    LOGGER.info("Initial Read until len: {} : {} ", feed.size(), feed.get(feed.size() - 1).getSequence());

    final Traffics self = this;
    final Completed c = streamies.new Completed(feed.get(feed.size() - 1)
        .getSequence()) {
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
    new Thread(new Fetchers(q, c)).start();
    new Thread(new Fetchers(q, c)).start();
    new Thread(new Fetchers(q, c)).start();
    new Thread(new Fetchers(q, c)).start();
    new Thread(new Fetchers(q, c)).start();
    new Thread(new Fetchers(q, c)).start();
    new Thread(new Fetchers(q, c)).start();
    new Thread(new Fetchers(q, c)).start();

  }

}