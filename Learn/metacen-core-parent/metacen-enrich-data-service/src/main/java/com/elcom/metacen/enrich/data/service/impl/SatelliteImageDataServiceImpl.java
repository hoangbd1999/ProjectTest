package com.elcom.metacen.enrich.data.service.impl;

import com.elcom.metacen.enrich.data.model.SatelliteImageData;
import com.elcom.metacen.enrich.data.repository.SatelliteImageDataRepository;
import com.elcom.metacen.enrich.data.service.SatelliteImageDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 *
 * @author Admin
 */
@Service
public class SatelliteImageDataServiceImpl implements SatelliteImageDataService {

//    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteImageDataServiceImpl.class);

    @Autowired
    SatelliteImageDataRepository satelliteImageDataRepository;

    @Override
    public List<SatelliteImageData> findByTileNumber(String tileNumber) {
        return satelliteImageDataRepository.findByTileNumber(tileNumber);
    }
}
