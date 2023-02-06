package com.elcom.vn.storage;

import com.elcom.util.app.QueueManager;
import com.elcom.util.miscellaneous.cfg.properties.SimpleConfig;
import com.elcom.util.queue.BoundBlockingQueue;
import com.elcom.vn.object.EventContainer;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

public class QueueMapManager {

    private static final QueueMapManager instance = new QueueMapManager();

    public static QueueMapManager getInstance() {
        return instance;
    }

    private QueueManager queue_manager = new QueueManager();
//  private MapManager map_manager = new MapManager();

    private BoundBlockingQueue<String> satellite_image_file_queue; //2
    
    private BoundBlockingQueue<EventContainer> vsat_queue; //5

//------------------------------------------------------------------------------
    public void reloadQueuesConfig(String cfgDir) throws IOException {
        queue_manager.reloadQueuesConfig(new File(cfgDir, "queue_size.cfg").getPath());
    }

    public void initQueues(String cfgDir) throws IOException {
        SimpleConfig queue_size_config = new SimpleConfig(new File(cfgDir, "queue_size.cfg").getPath()) {
            private static final long serialVersionUID = 1L;

            public String getTemplateConfigContent() {
                StringBuilder sbuf = new StringBuilder();

                sbuf.append("satellite_image_file" + "=" + 100000 + "\n"); //2
                sbuf.append("vsat" + "=" + 100000 + "\n"); //5

                return sbuf.toString();
            }
        };

        int satellite_image_file_queue_size = queue_size_config.getInt("satellite_image_file");
        satellite_image_file_queue = new BoundBlockingQueue(new ArrayBlockingQueue<>(satellite_image_file_queue_size)); //2
        satellite_image_file_queue.setName("satellite_image_file");
        satellite_image_file_queue.setMaxSize(satellite_image_file_queue_size);
        queue_manager.add(satellite_image_file_queue);

        int vsat_queue_size = queue_size_config.getInt("vsat");
        vsat_queue = new BoundBlockingQueue(new ArrayBlockingQueue<>(vsat_queue_size)); //5
        vsat_queue.setName("vsat");
        vsat_queue.setMaxSize(vsat_queue_size);
        queue_manager.add(vsat_queue);

        /**
         * RELOAD
         */
        reloadQueuesConfig(cfgDir);
    }

    public QueueManager getQueueManager() {
        return queue_manager;
    }

    public BoundBlockingQueue<String> getSatelliteImageFileQueue() {
        return satellite_image_file_queue;
    }

    public BoundBlockingQueue<EventContainer> getVsatQueue() {
        return vsat_queue;
    }
}
