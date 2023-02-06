package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.ObjectTypes;
import com.elcom.metacen.contact.model.dto.ObjectCriteria;
import com.elcom.metacen.contact.model.dto.ObjectGeneralInfoDTO;
import com.elcom.metacen.dto.redis.Countries;
import com.elcom.metacen.dto.redis.VsatDataSource;
import com.elcom.metacen.dto.redis.VsatVesselType;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
public interface CommonService {

    List<Countries> getListCountries();

    List<ObjectTypes> getListObjectTypes();

    Map<String, ObjectGeneralInfoDTO> buildObjectGeneralInfoMap(String destObjectType, List<String> destObjectIds);

    Page<ObjectGeneralInfoDTO> filterObject(ObjectCriteria objectCriteria);

    List<VsatVesselType> getListVesselType();

    List<VsatDataSource> getListVsatDataSource();
}
