package com.elcom.metacen.enrich.data.service;

import com.elcom.metacen.enrich.data.model.dto.VsatAisFilterListRequestDTO;
import com.elcom.metacen.enrich.data.model.dto.VsatAisResponseDTO;
import com.elcom.metacen.message.MessageContent;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
public interface VsatAisDataService {

    MessageContent searchAisListAllGeneral(VsatAisFilterListRequestDTO vsatAisFilterListRequestDTO);

    MessageContent findDetailVessel(Long mmsi);
}
