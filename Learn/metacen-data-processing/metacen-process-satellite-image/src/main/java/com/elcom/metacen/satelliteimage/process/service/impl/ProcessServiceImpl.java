package com.elcom.metacen.satelliteimage.process.service.impl;

import com.elcom.metacen.satelliteimage.process.model.kafka.consumer.SatelliteImageRawMessageFull;
import com.elcom.metacen.satelliteimage.process.model.kafka.producer.SatelliteImageRawMessageSimplify;
import com.elcom.metacen.satelliteimage.process.repository.SatelliteImageProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.elcom.metacen.satelliteimage.process.service.ProcessService;
import java.util.List;

/**
 *
 * @author Admin
 */
@Service
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private SatelliteImageProcessRepository satelliteImageProcessRepository;
    
    @Override
    public boolean insertSatelliteImageDataProcess(SatelliteImageRawMessageFull satelliteImage) {
        return this.satelliteImageProcessRepository.insertSatelliteImageDataProcess(satelliteImage);
    }
    
    @Override
    public boolean updateSatelliteImageRawProcessStatus(int processStatus, String satelliteImageRawUuidKey) {
        return this.satelliteImageProcessRepository.updateSatelliteImageRawProcessStatus(processStatus, satelliteImageRawUuidKey);
    }
    
    @Override
    public boolean updateSatelliteImageCompareProcessStatus(int processStatus, String satelliteImageCompareUuidKey) {
        return this.satelliteImageProcessRepository.updateSatelliteImageCompareProcessStatus(processStatus, satelliteImageCompareUuidKey);
    }
    
    @Override
    public List<SatelliteImageRawMessageSimplify> getLstSatelliteImageTimeoutProcess() {
        return this.satelliteImageProcessRepository.getLstSatelliteImageTimeoutProcess();
    }
    
    @Override
    public boolean increaseRetryTimesForSatelliteImage(String uuidKey) {
        return this.satelliteImageProcessRepository.increaseRetryTimesForSatelliteImage(uuidKey);
    }
}
