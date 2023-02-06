package com.elcom.metacen.satelliteimage.process.service;

import com.elcom.metacen.satelliteimage.process.model.kafka.consumer.SatelliteImageRawMessageFull;
import com.elcom.metacen.satelliteimage.process.model.kafka.producer.SatelliteImageRawMessageSimplify;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface ProcessService {
    
    boolean insertSatelliteImageDataProcess(SatelliteImageRawMessageFull satelliteImage);
    
    boolean updateSatelliteImageRawProcessStatus(int processStatus, String satelliteImageRawUuidKey);
    
    boolean updateSatelliteImageCompareProcessStatus(int processStatus, String satelliteImageCompareUuidKey);
    
    List<SatelliteImageRawMessageSimplify> getLstSatelliteImageTimeoutProcess();
    
    boolean increaseRetryTimesForSatelliteImage(String uuidKey);
}
