package com.elcom.metacen.vsat.media.process.service.impl;

import com.elcom.metacen.vsat.media.process.model.kafka.VsatMediaMessageFull;
import com.elcom.metacen.vsat.media.process.repository.VsatMediaProcessRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.elcom.metacen.vsat.media.process.service.ProcessService;
import java.util.List;

/**
 *
 * @author Admin
 */
@Service
public class ProcessServiceImpl implements ProcessService {

    @Autowired
    private VsatMediaProcessRepository vsatMediaProcessRepository;

//    @Override
//    public boolean updateProcessStatusForVsatMedia(int processStatus, Long processTime, String uuidKey) {
//        return vsatMediaProcessRepository.updateProcessStatusForVsatMedia(processStatus, processTime, uuidKey);
//    }

//    @Override
//    public boolean updateVsatMediaProcessSuccess(VsatMediaDTO vsatMediaDto) {
//        return vsatMediaProcessRepository.updateVsatMediaProcessSuccess(vsatMediaDto);
//    }

    /*@Override
    public boolean insertVsatMediaProcessedDetail(VsatMediaDTO vsatMediaDto) {
        return vsatMediaProcessRepository.insertVsatMediaProcessedDetail(vsatMediaDto);
    }*/

//    @Override
//    public boolean insertVsatMediaDataProcess(VsatMediaMessageFull vsatMediaMessage) {
//        return vsatMediaProcessRepository.insertVsatMediaDataProcess(vsatMediaMessage);
//    }

//    @Override
//    public boolean insertDataProcessRawStatus(List<DataProcessStatusDTO> input) {
//        return vsatMediaProcessRepository.insertDataProcessRawStatus(input);
//    }
    
    @Override
    public boolean insertVsatMediaDataLstProcess(List<VsatMediaMessageFull> vsatMediaMessages, Long processTime) {
        return vsatMediaProcessRepository.insertVsatMediaDataLstProcess(vsatMediaMessages, processTime);
    }

//    @Override
//    public List<VsatMediaMessageFull> getLstVsatMediaTimeoutProcess() {
//        return vsatMediaProcessRepository.getLstVsatMediaTimeoutProcess();
//    }

//    @Override
//    public boolean increaseRetryTimesForVsatMedia(String uuidKey) {
//        return this.vsatMediaProcessRepository.increaseRetryTimesForVsatMedia(uuidKey);
//    }
}
