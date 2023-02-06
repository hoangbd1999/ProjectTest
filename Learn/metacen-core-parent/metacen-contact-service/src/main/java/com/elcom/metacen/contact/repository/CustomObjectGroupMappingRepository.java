package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectGroupMapping;
import com.elcom.metacen.contact.model.dto.ObjectGroup.ObjectGroupMappingDTO;

import java.util.List;

/**
 *
 * @author Admin
 */
public interface CustomObjectGroupMappingRepository extends BaseCustomRepository<ObjectGroupMapping> {

    List<ObjectGroupMappingDTO> findByObjIdAndObjName(String term);

}
