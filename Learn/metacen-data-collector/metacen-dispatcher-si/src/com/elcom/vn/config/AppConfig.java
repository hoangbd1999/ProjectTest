package com.elcom.vn.config;

import com.elcom.vn.common.Constant;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.jconfig.Configuration;
import org.jconfig.ConfigurationManager;
import org.jconfig.ConfigurationManagerException;
import org.jconfig.handler.XMLFileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Admin
 */
public class AppConfig {
  
  private static Configuration jconfig = null;// ConfigurationManager.getConfiguration(RoutingConstant.CONFIG_NAME);
  public static Logger logger = LoggerFactory.getLogger(Constant.LOGGER_NAME);
  
  public AppConfig() {
  }
  
  public synchronized static Configuration getConfig() {
    if (jconfig == null) {
      jconfig = ConfigurationManager.getConfiguration(Constant.CONFIG_NAME);
    }
    return jconfig;
  }
  
  public static void reload() {
    synchronized (jconfig) {
      jconfig = ConfigurationManager.getConfiguration(Constant.CONFIG_NAME);
    }
  }
  
  public synchronized void reloadConfig(String cfgFileFullPath) {
    ConfigurationManager cm = ConfigurationManager.getInstance();
    try {
      File file = new File(cfgFileFullPath);
      if (!file.exists()) {
        createDefaultConfigFile(file);
      }
      XMLFileHandler fileHandler = new XMLFileHandler();
      fileHandler.setFile(file);
      cm.load(fileHandler, Constant.CONFIG_NAME);
    }
    catch (ConfigurationManagerException cme1) {
      logger.error("reloadConfig()", cme1);
    }
  }
  
  private void createDefaultConfigFile(File fout) {
    BufferedWriter w;
    try {
      w = new BufferedWriter(new FileWriter(fout));
      w.write("<?xml version=\"1.0\" ?>");
      w.newLine();
      w.write("\t<properties>");
      w.newLine();
      w.write("\t\t<category name=\"SETTINGS\">");
      w.newLine();
      w.write("\t\t\t<property name=\"DATA_MINING_PATH\" value=\"/opt/vsat/media_files\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"DATA_SOURCE\" value=\"123\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"SLEEP_TIME_MONITOR\" value=\"300000\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"SLEEP_TIME_GET_MON_INFO\" value=\"300000\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"IP_ADDRESS\" value=\"192.168.51.142\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"HARDWARE_NAME\" value=\"AGENT_1\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"MODULE_OANHNTK_NAME\" value=\"MONITOR_CLIENT\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"MODULE_DISPATCHER_NAME\" value=\"MONITOR_CLIENT\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"VSAT_SERVER_PORT\" value=\"8889\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"DEST_FOLDER_UPLOAD\" value=\"/opt/vsat/media_files\"/>");
      w.newLine();
      
      w.write("\t\t</category>");
      w.newLine();
      w.write("\t\t<category name=\"MONITOR_SERVER\">");
      w.newLine();
      w.write("\t\t\t<property name=\"HOST\" value=\"localhost\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"PORT\" value=\"9292\"/>");
      w.newLine();
      w.write("\t\t</category>");
      w.newLine();
      w.write("\t\t<category name=\"VSAT_RECEIVER\">");
      w.newLine();
      w.write("\t\t\t<property name=\"HOST\" value=\"localhost\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"PORT\" value=\"8886\"/>");
      w.newLine();
      w.write("\t\t</category>");
      w.newLine();
      
      w.write("\t\t<category name=\"FTP_SERVER\">");
      w.newLine();
      w.write("\t\t\t<property name=\"HOST\" value=\"localhost\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"PORT\" value=\"21\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"USERNAME\" value=\"9292\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"PASSWORD\" value=\"9292\"/>");
      w.newLine();
      w.write("\t\t</category>");
      w.newLine();
      
      w.write("\t\t<category name=\"SSL\">");
      w.newLine();
      w.write("\t\t\t<property name=\"KeyStoreFile\" value=\"10\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"TrustStoreFile\" value=\"10\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"KeyStorePassword\" value=\"10\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"TrustStorePassword\" value=\"10\"/>");
      w.newLine();
      w.write("\t\t\t<property name=\"CipherSuites\" value=\"10\"/>");
      w.newLine();
      w.write("\t\t</category>");
      w.newLine();
      w.write("\t</properties>");
      w.flush();
      w.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }
}
