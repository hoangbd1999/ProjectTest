package com.elcom.metacen.enrich.data.service;

import com.elcom.metacen.enrich.data.model.SatelliteImageObjectAnalyzed;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface SatelliteImageObjectAnalyzedService {

    List<SatelliteImageObjectAnalyzed> satelliteImageUuidKey(String uuid);

    SatelliteImageObjectAnalyzed findByUuid(String uuid);

    SatelliteImageObjectAnalyzed delete(int isDeleted, String uuid);
}
