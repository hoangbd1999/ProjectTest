package com.elcom.vn.processor.scanner;

import com.elcom.util.queue.BoundBlockingQueue;
import java.io.File;
import org.slf4j.Logger;

public class SatelliteImageFolderScanner extends AbstractFolderScanner {

    public SatelliteImageFolderScanner(String name, Logger logger, String dataMiningDirPath
                            , int lightlyNumberQueueOfMediaMainProgress
                            , BoundBlockingQueue<String> decodeFileQueue) {
        super(name, logger, dataMiningDirPath, lightlyNumberQueueOfMediaMainProgress, ".", false, decodeFileQueue);
    }

    @Override
    public boolean accept(File dir, String name) {
        return true;
    }
}
