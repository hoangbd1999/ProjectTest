package com.elcom.metacen.vsat.media.process.service;

import com.elcom.metacen.vsat.media.process.model.kafka.VsatMediaMessageFull;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface ProcessService {

    // boolean updateProcessStatusForVsatMedia(int processStatus, Long processTime, String uuidKey);

    // boolean updateVsatMediaProcessSuccess(VsatMediaDTO vsatMediaDto);

    // boolean insertVsatMediaProcessedDetail(VsatMediaDTO vsatMediaDto);

    // boolean insertVsatMediaDataProcess(VsatMediaMessageFull vsatMediaMessage);
    
//    boolean insertDataProcessRawStatus(List<DataProcessStatusDTO> input);

    boolean insertVsatMediaDataLstProcess(List<VsatMediaMessageFull> vsatMediaMessage, Long processTime);

    // List<VsatMediaMessageFull> getLstVsatMediaTimeoutProcess();

    // boolean increaseRetryTimesForVsatMedia(String uuidKey);
}
