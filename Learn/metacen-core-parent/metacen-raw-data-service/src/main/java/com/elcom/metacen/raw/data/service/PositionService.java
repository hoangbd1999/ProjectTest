package com.elcom.metacen.raw.data.service;

import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.raw.data.model.dto.AisDataDTO;
import com.elcom.metacen.raw.data.model.dto.AisDataFilterDTO;
import com.elcom.metacen.raw.data.model.dto.PositionOverallRequest;
import com.elcom.metacen.raw.data.model.dto.PositionResponseDTO;
import com.elcom.metacen.raw.data.model.dto.VsatAisFilterDTO;
import java.math.BigInteger;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
public interface PositionService {

    MessageContent filterVsatAisRawData(VsatAisFilterDTO input);
    
    MessageContent filterAisRawData(AisDataFilterDTO input);

    Page<AisDataDTO> filterAisMapping(Integer page, Integer size, String term);
    
    List<PositionResponseDTO> findPositionOverallFromVsatSystem(PositionOverallRequest input);
    
    List<PositionResponseDTO> findPositionOverallFromAisSystem(PositionOverallRequest input);
    
    List<Long> findObjLstOnMedia(String fromTime, String toTime, List<Integer> mediaTypes, List<String> fileTypes);
}
