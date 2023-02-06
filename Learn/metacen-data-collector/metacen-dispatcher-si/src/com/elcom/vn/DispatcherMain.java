package com.elcom.vn;

import com.elcom.util.app.AppReport;
import com.elcom.util.app.IApplication;
import com.elcom.vn.common.Constant;
import com.elcom.vn.config.AppConfig;
import com.elcom.vn.processor.cleaner.CleanFolderScanner;
import com.elcom.vn.processor.decoder.SatelliteImageFileProcessor;
import com.elcom.vn.processor.scanner.SatelliteImageFolderScanner;
import com.elcom.vn.processor.sender.SendProcessor;
import com.elcom.vn.storage.QueueMapManager;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    Scan và xử lý folder ảnh vệ tinh
*/
public class DispatcherMain implements IApplication {
    
    private static final Logger MAIN_LOGGER = LoggerFactory.getLogger("main");

    private static final Logger PROCESSING_LOGGER = LoggerFactory.getLogger("processing");

    public static String DATA_VENDOR;
    
    public static String SATELLITE_NAME;

    public static String GEOSERVER_URL;
    
    public static String GEOSERVER_WORKSPACE;
    
    public static String DEST_UPLOAD_PATH;
    
    private static DispatcherMain MAIN_APP;

    public static DispatcherMain getMainApp() {
        return MAIN_APP;
    }
    
    // a class that extends thread that is to be called when program is exiting
    static class Message extends Thread {

        @Override
        public void run() {
            MAIN_LOGGER.info("Dispatcher is shutdown!");
        }
    }

    private volatile boolean force_stop;
    private String cfg_dir;
    private int app_index;
    private String app_name;

    private DispatcherMain() {
    }

    public static void main(String[] args) {
        try {
            
            // register Message as shutdown hook
            Runtime.getRuntime().addShutdownHook(new Message());
            
            new DispatcherMain().run(args);
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("The program has exited.");
            System.exit(-1);
        }
    }

  private void run(String[] args) throws Throwable {
      
    app_index = 0;
    app_name = "dispatcher0";
    
    //cfg_dir = System.getProperty("user.home") + "/config/dispatcher";
    cfg_dir = "./config/";

    if (args.length > 0)
      app_index = Integer.parseInt(args[0]);
    
    if (args.length > 1)
      app_name = args[1];

     if (args.length > 2)
       cfg_dir = args[2];

    DOMConfigurator.configureAndWatch(new File(cfg_dir, "LogConfig.log4j.xml").getPath());
    MAIN_LOGGER.info(".............................................");
    MAIN_LOGGER.info(".");
    MAIN_LOGGER.info("Start module at " + new Date() + ", cfg_dir:["+cfg_dir+"], args:["+Arrays.toString(args)+"]");
    MAIN_LOGGER.info(".");
    MAIN_LOGGER.info(".............................................");

    /**
     * STEP 1: RELOAD CONFIG
     */
    AppConfig config = new AppConfig();
    
    config.reloadConfig(new File(cfg_dir, Constant.CONFIG_FILE_NAME).getPath());

    DATA_VENDOR = AppConfig.getConfig().getProperty("DATA_VENDOR", "", "SETTINGS");
    
    SATELLITE_NAME = AppConfig.getConfig().getProperty("SATELLITE_NAME", "", "SETTINGS");
    
    GEOSERVER_URL = AppConfig.getConfig().getProperty("GEOSERVER_URL", "", "SETTINGS");
    
    GEOSERVER_WORKSPACE = AppConfig.getConfig().getProperty("GEOSERVER_WORKSPACE", "", "SETTINGS");
    
    DEST_UPLOAD_PATH = AppConfig.getConfig().getProperty("DEST_UPLOAD_PATH", "", "SETTINGS");
    
    /**
     * STEP 2: INIT ALL QUEUE + MAP
     */
    QueueMapManager.getInstance().initQueues(cfg_dir);
//    QueueMapManager.getInstance().initMaps(cfg_dir);

    Thread.sleep(5000);
    
    /**
     * STEP 5: RUN ALL THREAD
     */
    String dataMiningDirPath = AppConfig.getConfig().getProperty("DATA_MINING_PATH", "/ttttbien2/metacen/satellite-images/bienDong_infor", "SETTINGS");
    
    String cleanFolderDirPath = AppConfig.getConfig().getProperty("CLEAN_FOLDER_PATH", "/ttttbien2/metacen/satellite-images/bienDong", "SETTINGS");
    
    new CleanFolderScanner(CleanFolderScanner.class.getName(), PROCESSING_LOGGER, cleanFolderDirPath)
                        .execute();
    
    Thread.sleep(1000);
    
    int maxFilesPerScan = AppConfig.getConfig().getIntProperty("MAX_FILES_PER_SCAN", 4000, "SETTINGS");
    
    MAIN_LOGGER.info("FolderScanner is started!");
    SatelliteImageFolderScanner folderScanner = new SatelliteImageFolderScanner(SatelliteImageFolderScanner.class.getName(), PROCESSING_LOGGER, dataMiningDirPath
                                                                , maxFilesPerScan, QueueMapManager.getInstance().getSatelliteImageFileQueue());
    folderScanner.execute();

    // File processing
    SAXParserFactory saxpf = SAXParserFactory.newInstance();
    SAXParser saxParser;
    
    int numOfThreadDecode = AppConfig.getConfig().getIntProperty("NUM_THREAD_DECODE", 4, "SETTINGS");
    for (int i = 0; i < numOfThreadDecode; i++) {

        saxParser = saxpf.newSAXParser();

        SatelliteImageFileProcessor fileProcessor = new SatelliteImageFileProcessor(SatelliteImageFileProcessor.class.getName() + "_" + i, PROCESSING_LOGGER
                                                                            , null, null, null, saxParser, QueueMapManager.getInstance().getVsatQueue()
                                                                            , QueueMapManager.getInstance().getSatelliteImageFileQueue());
        fileProcessor.execute();
    }

    int numOfThreadSend = AppConfig.getConfig().getIntProperty("NUM_THREAD_SEND", 1, "SETTINGS");
    for (int i = 0; i < numOfThreadSend; i++) {
        SendProcessor sendProcessor = new SendProcessor(SendProcessor.class.getName() + "_" + i, PROCESSING_LOGGER, QueueMapManager.getInstance().getVsatQueue());
        sendProcessor.execute();
    }
    
    /**
     * STEP 7: REPORT 
     */
    Timer reportTimer = new Timer("REPORT_TIMER");
    reportTimer.schedule(new TimerTask() {
      public void run() {
        try {
          /**
           * MEM + THREAD
           */
          StringBuilder membuf = new StringBuilder();
          AppReport.memory_report(membuf);
          MAIN_LOGGER.info(membuf.toString());

          /**
           * QUEUE
           */
          StringBuilder queuebuf = new StringBuilder();
          QueueMapManager.getInstance().getQueueManager().queuesReport(queuebuf);
          MAIN_LOGGER.info(queuebuf.toString());

          /**
           * MAP
           */
//          StringBuilder mapbuf = new StringBuilder();
//          QueueMapManager.getInstance().getMapManager().mapsReport(mapbuf);
//          MAIN_LOGGER.info(mapbuf.toString());
        }
        catch (Throwable t) {
          MAIN_LOGGER.error("Error while reporting", t);
        }
      }
    }, 30000L, 30000L);

    /**
     * SLEEP FOREVER
     */
    Thread.sleep(Long.MAX_VALUE);
  }

//------------------------------------------------------------------------------
  public String get_info(String[] params) throws Throwable {
      return null;
//    StringBuilder sbuf = new StringBuilder(1024*1024);
//    String sub_module = params.length > 0 ? params[0] : "";
//    String[] infs;
//
//    if ("event_producer_server".equalsIgnoreCase(sub_module)) {
//      if (params.length > 1) {
//        String servicename = params[1];
//        infs = event_producer_server.getServiceManager().getSessionsInfos(servicename);
//      }
//      else {
//        infs = event_producer_server.getServiceManager().getSessionsInfos();
//      }
//    }
//    else {
//      infs = new String[] {
//          get_info()
//      };
//    }
//
//    if (infs != null) {
//      for (String inf : infs) {
//        sbuf.append(inf);
//        sbuf.append("\n");
//      }
//    }
//
//    return sbuf.toString();
  }

//------------------------------------------------------------------------------
//  private String get_info() throws Throwable {
//    StringBuilder sbuf = new StringBuilder();
//
//    AppReport.memory_report(sbuf);
//    QueueMapManager.getInstance().getQueueManager().queuesReport(sbuf);
//    QueueMapManager.getInstance().getMapManager().mapsReport(sbuf);
//
//    return sbuf.toString();
//  }

//------------------------------------------------------------------------------
  public void reload_config(String[] params) throws Throwable {
  }

//------------------------------------------------------------------------------
//  private void reload_queue_size() throws Throwable {
//    QueueMapManager.getInstance().reloadQueuesConfig(cfg_dir);
//  }

//------------------------------------------------------------------------------
  public void set_name(String app_name) {
    this.app_name = app_name;
  }
  public String get_name() {
    return app_name;
  }

//------------------------------------------------------------------------------
  public void set_index(int app_index) {
    this.app_index = app_index;
  }
  public int get_index() {
    return app_index;
  }

//------------------------------------------------------------------------------
  public void start(String[] params) throws Throwable {
  }
  public void stop(String[] params) throws Throwable {
  }  
  public void restart(String[] params) throws Throwable {
  }

//------------------------------------------------------------------------------
  public String get_cfg_dir() {
    return cfg_dir;
  }
  public void set_cfg_dir(String cfg_dir) {
    this.cfg_dir = cfg_dir;
  }

//------------------------------------------------------------------------------
  public boolean isRunning() {
    return !force_stop;
  }

//------------------------------------------------------------------------------
  public void other_command(String[] in, String[] out) throws Throwable {
    out[0] = "Not support this command";
  }
}
