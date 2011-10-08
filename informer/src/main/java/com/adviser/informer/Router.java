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
import org.w3c.dom.Document;

import com.adviser.informer.model.Streamies;
import com.adviser.informer.model.TopItem;
import com.adviser.informer.model.streamie.AggregatedHistory;
import com.adviser.informer.model.streamie.Streamie;
import com.adviser.informer.model.streamie.StreamieContainer;
import com.adviser.informer.model.streamie.StreamieLocation;

public class Router extends RouteBuilder {
  public class ListenAddress {
    private String addr = "127.0.0.1";
    private String port = "2911";

    public ListenAddress() {
    }

    public ListenAddress(String port, String addr) {
      this.port = port;
      if (addr != null)
        this.addr = addr;
    }

    public String toString() {
      return addr + ":" + port;
    }
  }

  private ListenAddress listenaddress = null;

  private Streamies streamies = null;

  public Router(String[] args, Streamies _streamies) {
    streamies = _streamies;
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
    System.out.println("Version:" + getServer());
    System.out.println("Listen On:" + listenaddress.toString());
    from(
        "restlet:http://" + listenaddress.toString()
            + "/traffic/byTwitter/{screenName}?restletMethods=get").bean(this,
        "byTwitter");
    from(
        "restlet:http://" + listenaddress.toString()
            + "/traffic/top10?restletMethods=get").bean(this, "top10");
    from(
        "restlet:http://" + listenaddress.toString()
            + "/traffic/total/{screenName}?restletMethods=get").bean(this, "totalByScreenName");
    from(
        "restlet:http://" + listenaddress.toString()
            + "/traffic/total?restletMethods=get").bean(this, "total");
    from(
        "restlet:http://" + listenaddress.toString()
            + "/streamies?restletMethods=get").bean(this, "streamies");
  }

  private String _version = null;

  private String getServer() {
    if (_version != null) {
      return _version;
    }
    synchronized (this) {
      if (_version != null) {
        return _version;
      }
      String version = "Informer(development)";
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
          // System.out.println("IS:"+e.getMessage());
        }
      }
      _version = version;
    }
    return _version;
  }

 // private String _streamies;
 // private long   _updateStreamies;
  public void streamies(Exchange exchange) {
    final Message _out = exchange.getOut();
    _out.setHeader("Content-Type", "text/javascript");
    final Map<String, String> map = exchange.getIn()
    .getHeader("CamelRestletRequest", Request.class).getResourceRef()
    .getQueryAsForm().getValuesMap();
    StringWriter str = new StringWriter();
    preJsonP(map, str);
    try {
 //     if (_updateStreamies == null || (new _updateStreamies)
      final LinkedList<StreamieLocation> ret = new LinkedList<StreamieLocation>();
      final Iterator<StreamieContainer> i = streamies.getById().values().iterator();
      while (i.hasNext()) {
        final StreamieContainer sc = i.next();
        ret.push(new StreamieLocation(sc.getStreamie()));
      }
      (new ObjectMapper()).writeValue(str, ret);
    } catch (Exception e) {
      e.printStackTrace();
    }
    postJsonP(map, str);
    //System.out.println(str.toString());
    _out.setBody(str.toString());
  }
  
  public void byTwitter(Exchange exchange) {
    final Message _in = exchange.getIn();
    final String screenName = _in.getHeader("screenName", String.class);
    final Message _out = exchange.getOut();
    _out.setHeader("Content-Type", "text/javascript");
    Streamie streamie = streamies.findByTwitter(screenName).getStreamie();
    final Map<String, String> map = exchange.getIn()
    .getHeader("CamelRestletRequest", Request.class).getResourceRef()
    .getQueryAsForm().getValuesMap();
    StringWriter str = new StringWriter();
    preJsonP(map, str);
    try {
      (new ObjectMapper()).writeValue(str, streamie);
    } catch (Exception e) {
      e.printStackTrace();
    }
    postJsonP(map, str);
    _out.setBody(str.toString());
  }

  public void total(Exchange exchange) {
    final Message _out = exchange.getOut();
    _out.setHeader("Content-Type", "text/javascript");
    final Map<String, String> map = exchange.getIn()
        .getHeader("CamelRestletRequest", Request.class).getResourceRef()
        .getQueryAsForm().getValuesMap();
    StringWriter str = new StringWriter();
    preJsonP(map, str);
    try {
      (new ObjectMapper()).writeValue(str, new AggregatedHistory(streamies.getTotalTraffic()));
    } catch (Exception e) {
      e.printStackTrace();
    }
    postJsonP(map, str);
    _out.setBody(str.toString());
  }
  
  public void totalByScreenName(Exchange exchange) {
    final Message _out = exchange.getOut();
    _out.setHeader("Content-Type", "text/javascript");
    final Map<String, String> map = exchange.getIn()
        .getHeader("CamelRestletRequest", Request.class).getResourceRef()
        .getQueryAsForm().getValuesMap();
 
    final String screenName = exchange.getIn().getHeader("screenName", String.class);
    StringWriter str = new StringWriter();
    preJsonP(map, str);
    try {
     (new ObjectMapper()).writeValue(str, new AggregatedHistory(streamies.findByTwitter(screenName).getTrafficContainer().getTraffic()));
    } catch (Exception e) {
      e.printStackTrace();
    }
    postJsonP(map, str);
    _out.setBody(str.toString());
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
  public void top10(Exchange exchange) {
    final Message _out = exchange.getOut();
    _out.setHeader("Content-Type", "text/javascript");
    final Map<String, String> map = exchange.getIn()
        .getHeader("CamelRestletRequest", Request.class).getResourceRef()
        .getQueryAsForm().getValuesMap();
    final String o = map.get("count");
    int count = 10;
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
      e.printStackTrace();
    }
    postJsonP(map, str);
    _out.setBody(str.toString());
  }
}
