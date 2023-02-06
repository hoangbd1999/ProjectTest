package com.elcom.metacen.enrich.data.service;

import com.elcom.metacen.enrich.data.model.SatelliteImageData;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface SatelliteImageDataService {

    List<SatelliteImageData> findByTileNumber(String tileNumber);
}
