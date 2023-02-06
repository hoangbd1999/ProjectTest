package com.elcom.metacen.enrich.data.service.impl;

import com.elcom.metacen.enrich.data.model.SatelliteImageChanges;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeFilterDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeRequestDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeResponseDTO;
import com.elcom.metacen.enrich.data.repository.SatelliteImageChangeRepository;
import com.elcom.metacen.enrich.data.service.SatelliteImageChangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


/**
 *
 * @author Admin
 */
@Service
public class SatelliteImageChangeServiceImpl implements SatelliteImageChangeService {

//    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageChangeServiceImpl.class);

    @Autowired
    SatelliteImageChangeRepository satelliteImageChangeRepository;

    @Override
    public SatelliteImageChanges save(SatelliteImageChangeRequestDTO satelliteImageChangeRequestDTO) {
        return satelliteImageChangeRepository.insert(satelliteImageChangeRequestDTO);
    }

    @Override
    public Page<SatelliteImageChangeResponseDTO> filterSatelliteImageChange(SatelliteImageChangeFilterDTO data) {
        return satelliteImageChangeRepository.filterSatelliteImageChange(data);
    }

    @Override
    public SatelliteImageChanges findByUuid(String uuid) {
        return satelliteImageChangeRepository.findByUuid(uuid);
    }

    @Override
    public SatelliteImageChanges delete(int isDeleted, String uuid) {
        return satelliteImageChangeRepository.delete(isDeleted, uuid);
    }

}
