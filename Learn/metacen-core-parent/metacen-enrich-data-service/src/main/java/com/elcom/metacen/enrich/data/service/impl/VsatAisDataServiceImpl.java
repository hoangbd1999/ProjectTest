package com.elcom.metacen.enrich.data.service.impl;

import com.elcom.metacen.enrich.data.model.dto.VsatAisFilterListRequestDTO;
import com.elcom.metacen.enrich.data.model.dto.VsatAisResponseDTO;
import com.elcom.metacen.enrich.data.repository.VsatAisDataRepository;
import com.elcom.metacen.enrich.data.service.VsatAisDataService;
import com.elcom.metacen.message.MessageContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * @author Admin
 */
@Service
public class VsatAisDataServiceImpl implements VsatAisDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VsatAisDataServiceImpl.class);

    @Autowired
    VsatAisDataRepository vsatAisDataRepository;

    @Override
    public MessageContent searchAisListAllGeneral(VsatAisFilterListRequestDTO vsatAisFilterListRequestDTO) {
        try {
            return vsatAisDataRepository.searchAisListAllGeneral(vsatAisFilterListRequestDTO);
        } catch (Exception e) {
            LOGGER.error("filter failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public MessageContent findDetailVessel(Long mmsi) {
        try {
            return vsatAisDataRepository.findDetailVessel(mmsi);
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }
}
