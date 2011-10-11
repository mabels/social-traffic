package com.adviser.informer.model;

import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Configuration {
  private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);


  private static Properties _properties = null;

  public static String get(String p) {
    return get(p, "");
  }

  public static String get(String p, String def) {
    return load().getProperty(p, def);
  }

  public static Properties load() {
    return load(null);
  }

  public static Properties load(String name) {
    if (_properties != null)
      return _properties;
    _properties = new Properties();
    final String fname = name + ".properties";
    try {
      _properties.load(new FileInputStream(fname));
      LOGGER.info("loaded " + fname);
    } catch (Exception e) {
      LOGGER.error("load of {} failed: {}", fname, e.getMessage());
    }
    return _properties;
  }

}
