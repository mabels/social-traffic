package com.adviser.informer;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.adviser.informer.model.Streamies;
import com.adviser.informer.model.TopItem;
import com.adviser.informer.model.streamie.AggregatedHistory;
import com.adviser.informer.model.streamie.Streamie;
import com.adviser.informer.model.streamie.StreamieContainer;
import com.adviser.informer.model.streamie.StreamieLocation;

public class Router extends RouteBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(Router.class);

  public class ListenAddress {
    private String addr = "127.0.0.1";
    private String port = "2911";

    public ListenAddress() {
    }

    public ListenAddress(String port, String addr) {
      this.port = port;
      if (addr != null) {
        this.addr = addr;
      }
    }

    public String toString() {
      return addr + ":" + port;
    }
  }

  private ListenAddress listenaddress = null;

  private Streamies streamies = null;

  public Router(String[] args, Streamies streamies) {
    this.streamies = streamies;
    if (args.length == 1) {
      listenaddress = new ListenAddress(args[0], null);
    } else if (args.length >= 2) {
      listenaddress = new ListenAddress(args[0], args[1]);
    } else {
      listenaddress = new ListenAddress();
    }
    init();
  }

  private void init() {
  }

  public void configure() {
    final String restlet = "restlet:http://";
    LOGGER.info("Version: {}", getServer());
    LOGGER.info("Listen On: {}", listenaddress.toString());
    from(
        restlet + listenaddress.toString()
            + "/traffic/byTwitter/{screenName}?restletMethods=get").bean(this,
        "byTwitter");
    from(
        restlet + listenaddress.toString()
            + "/traffic/top10?restletMethods=get").bean(this, "top10");
    from(
        restlet + listenaddress.toString()
            + "/traffic/total/{screenName}?restletMethods=get").bean(this,
        "totalByScreenName");
    from(
        restlet + listenaddress.toString()
            + "/traffic/total?restletMethods=get").bean(this, "total");
    from(restlet + listenaddress.toString() + "/streamies?restletMethods=get")
        .bean(this, "streamies");
  }

  private String version = null;

  private String getServer() {
    if (version != null) {
      return version;
    }
    synchronized (this) {
      if (version != null) {
        return version;
      }
      version = "Informer(development)";
      final InputStream is = Router.class.getClassLoader().getResourceAsStream(
          "META-INF/maven/com.adviser.informer/informer/pom.xml");
      if (is != null) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
          DocumentBuilder db = dbf.newDocumentBuilder();
          doc = db.parse(is);
          version = "Informer("
              + doc.getElementsByTagName("version").item(0).getTextContent()
              + ")";
        } catch (Exception e) {
          LOGGER.error("Router error:", e);
          // System.out.println("IS:"+e.getMessage());
        }
      }
    }
    return version;
  }

  // private String _streamies;
  // private long _updateStreamies;
  public void streamies(Exchange exchange) {
    final Message out = exchange.getOut();
    out.setHeader("Content-Type", "text/javascript");
    final Map<String, String> map = exchange.getIn()
        .getHeader("CamelRestletRequest", Request.class).getResourceRef()
        .getQueryAsForm().getValuesMap();
    StringWriter str = new StringWriter();
    preJsonP(map, str);
    try {
      // if (_updateStreamies == null || (new _updateStreamies)
      final LinkedList<StreamieLocation> ret = new LinkedList<StreamieLocation>();
      final Iterator<StreamieContainer> i = streamies.getById().values()
          .iterator();
      while (i.hasNext()) {
        final StreamieContainer sc = i.next();
        ret.push(new StreamieLocation(sc.getStreamie()));
      }
      (new ObjectMapper()).writeValue(str, ret);
    } catch (Exception e) {
      LOGGER.error("streamies:",e);
    }
    postJsonP(map, str);
    // System.out.println(str.toString());
    out.setBody(str.toString());
  }

  public static Map<String, String> getHeaders(Message in) {
    return in.getHeader("CamelRestletRequest", Request.class).getResourceRef()
        .getQueryAsForm().getValuesMap();
  }

  public void byTwitter(Exchange exchange) {
    final Message in = exchange.getIn();
    final String screenName = in.getHeader("screenName", String.class);
    final Message out = exchange.getOut();
    out.setHeader("Content-Type", "text/javascript");
    Streamie streamie = streamies.findByTwitter(screenName).getStreamie();
    final Map<String, String> map = getHeaders(in);
    final StringWriter str = new StringWriter();
    preJsonP(map, str);
    try {
      (new ObjectMapper()).writeValue(str, streamie);
    } catch (Exception e) {
      LOGGER.error("byTwitter:",e);
    }
    postJsonP(map, str);
    out.setBody(str.toString());
  }

  public void total(Exchange exchange) {
    final Message out = exchange.getOut();
    out.setHeader("Content-Type", "text/javascript");
    final Map<String, String> map = getHeaders(exchange.getIn());
    final StringWriter str = new StringWriter();
    preJsonP(map, str);
    try {
      (new ObjectMapper()).writeValue(str,
          new AggregatedHistory(streamies.getTotalTraffic()));
    } catch (Exception e) {
      LOGGER.error("total:",e);
    }
    postJsonP(map, str);
    out.setBody(str.toString());
  }

  private static void contentTypeJavascript(Message out) {
    out.setHeader("Content-Type", "text/javascript");
  }

  public void totalByScreenName(Exchange exchange) {
    final Message out = exchange.getOut();
    contentTypeJavascript(out);
    final Map<String, String> map = exchange.getIn()
        .getHeader("CamelRestletRequest", Request.class).getResourceRef()
        .getQueryAsForm().getValuesMap();

    final String screenName = exchange.getIn().getHeader("screenName",
        String.class);
    StringWriter str = new StringWriter();
    preJsonP(map, str);
    try {
      (new ObjectMapper()).writeValue(str, new AggregatedHistory(streamies
          .findByTwitter(screenName).getTrafficContainer().getTraffic()));
    } catch (Exception e) {
      LOGGER.error("totalByScreenName:",e);
    }
    postJsonP(map, str);
    out.setBody(str.toString());
  }

  private void preJsonP(Map<String, String> map, StringWriter str) {
    final String callback = map.get("callback");
    if (callback != null) {
      str.append(callback);
      str.append("(");
    }
  }

  private void postJsonP(Map<String, String> map, StringWriter str) {
    final String callback = map.get("callback");
    if (callback != null) {
      str.append(")");
    }
  }

  private final static int DEFAULT_TOP10 = 10;

  public void top10(Exchange exchange) {
    final Message out = exchange.getOut();
    contentTypeJavascript(out);
    final Map<String, String> map = exchange.getIn()
        .getHeader("CamelRestletRequest", Request.class).getResourceRef()
        .getQueryAsForm().getValuesMap();
    final String o = map.get("count");
    int count = DEFAULT_TOP10;
    if (o != null) {
      count = Integer.parseInt(o);
    }
    StringWriter str = new StringWriter();
    preJsonP(map, str);
    List<TopItem> topItems = new LinkedList<TopItem>();
    Iterator<StreamieContainer> i = streamies.top(count).iterator();
    while (i.hasNext()) {
      topItems.add(new TopItem(i.next()));
    }
    try {
      (new ObjectMapper()).writeValue(str, topItems);
    } catch (Exception e) {
      LOGGER.error("top10:",e);
    }
    postJsonP(map, str);
    out.setBody(str.toString());
  }
}
