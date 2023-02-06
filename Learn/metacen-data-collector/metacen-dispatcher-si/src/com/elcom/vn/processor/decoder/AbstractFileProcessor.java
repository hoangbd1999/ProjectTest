package com.elcom.vn.processor.decoder;

import com.elcom.util.miscellaneous.thread.ActionThread;
import com.elcom.util.queue.BoundBlockingQueue;
import com.elcom.vn.object.EventContainer;
import com.google.gson.Gson;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class AbstractFileProcessor extends ActionThread {
  protected static final Gson gson = new Gson();

  protected final Logger logger;
  protected final BoundBlockingQueue<EventContainer> vsatQueue;
  protected final BoundBlockingQueue<String> decodeFileQueue;

//------------------------------------------------------------------------------
  public AbstractFileProcessor(String name, Logger logger, BoundBlockingQueue<EventContainer> vsatQueue, BoundBlockingQueue<String> decodeFileQueue) {
    super(name);

    this.logger = logger;
    this.vsatQueue = vsatQueue;
    this.decodeFileQueue = decodeFileQueue;
  }

//-----------------------------------------------------------------------------
  protected void onThrowable(Throwable t) {
    if(logger.isErrorEnabled())logger.error("Unexpected throwable", t);
  }

//-----------------------------------------------------------------------------
  protected void onExecuting() throws Throwable {
    if(logger.isInfoEnabled())logger.info("Running...");
  }

//-----------------------------------------------------------------------------
  protected void onKilling() {
    if(logger.isInfoEnabled())logger.info("Terminated.");
  }

//------------------------------------------------------------------------------
  protected long sleeptime() throws Throwable {
    return -1;
  }

//------------------------------------------------------------------------------
  protected void action() throws Exception {
    try {
      childAction();
    }
    catch (Throwable ex) {
      logger.error("Err: " + (ex.getMessage() != null ? ex.getMessage() : ex), ex);
    }
  }

//------------------------------------------------------------------------------
  protected abstract void childAction() throws Throwable;

//------------------------------------------------------------------------------
  protected String getValueByElementName(String element, NodeList ndLst) {
    String s = null;
    try {
      s = ((Element)((Element)ndLst.item(0)).getElementsByTagName(element).item(0)).getElementsByTagName("Value").item(0).getTextContent();
    }
    catch (Exception ex) {
      logger.error("Err: " + ex.getMessage(), ex);
    }
    return s;
  }

//------------------------------------------------------------------------------
  protected void enqueue(EventContainer e, BoundBlockingQueue<EventContainer> queue) {
    try {
      boolean ok = false;
      do {
        ok = queue.offer(e, 250L, TimeUnit.MILLISECONDS);
      }
      while (!ok);
    }
    catch (InterruptedException ex) {
      logger.warn("InterruptedException while enqueue: " + e);
    }
  }

//------------------------------------------------------------------------------
  public static void main(String[] args) {
    String o = "x:/vsatdispatcher.log.l1.txt";
    String n = FilenameUtils.removeExtension(o);

    System.out.println("OLD=" + o);
    System.out.println("NEW=" + n);
  }
  
    protected static String getElapsedTime(long miliseconds) {
        return miliseconds + " (ms)";
    }
}
