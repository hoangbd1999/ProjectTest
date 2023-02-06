package com.elcom.metacen.raw.data.service.impl;

import com.elcom.metacen.raw.data.model.dto.VsatAisFilterDTO;
import com.elcom.metacen.raw.data.repository.VsatAisDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.raw.data.model.dto.AisDataDTO;
import com.elcom.metacen.raw.data.model.dto.AisDataFilterDTO;
import com.elcom.metacen.raw.data.model.dto.PositionOverallRequest;
import com.elcom.metacen.raw.data.model.dto.PositionResponseDTO;
import com.elcom.metacen.raw.data.repository.AisDataRepository;
import com.elcom.metacen.raw.data.repository.PositionRepository;
import com.elcom.metacen.raw.data.repository.VsatMediaDataRepository;
import com.elcom.metacen.raw.data.service.PositionService;
import java.math.BigInteger;
import java.util.List;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
@Service
public class PositionServiceImpl implements PositionService {

    @Autowired
    VsatAisDataRepository vsatAisDataRepository;
    
    @Autowired
    AisDataRepository aisDataRepository;
    
    @Autowired
    PositionRepository positionRepository;
    
    @Autowired
    VsatMediaDataRepository vsatMediaDataRepository;

    @Override
    public MessageContent filterVsatAisRawData(VsatAisFilterDTO input) {
        return this.vsatAisDataRepository.filterVsatAisRawData(input);
    }
    
    @Override
    public MessageContent filterAisRawData(AisDataFilterDTO input) {
        return this.aisDataRepository.filterAisRawData(input);
    }

    @Override
    public Page<AisDataDTO> filterAisMapping(Integer page, Integer size, String term) {
        return this.aisDataRepository.filterAisMapping(page, size, term);
    }
    
    @Override
    public List<PositionResponseDTO> findPositionOverallFromVsatSystem(PositionOverallRequest input) {
        return this.positionRepository.findPositionOverallFromVsatSystem(input);
    }
    
    @Override
    public List<PositionResponseDTO> findPositionOverallFromAisSystem(PositionOverallRequest input) {
        return this.positionRepository.findPositionOverallFromAisSystem(input);
    }
    
    @Override
    public List<Long> findObjLstOnMedia(String fromTime, String toTime, List<Integer> mediaTypes, List<String> fileTypes) {
        return this.vsatMediaDataRepository.findObjLstOnMedia(fromTime, toTime, mediaTypes, fileTypes);
    }
}
