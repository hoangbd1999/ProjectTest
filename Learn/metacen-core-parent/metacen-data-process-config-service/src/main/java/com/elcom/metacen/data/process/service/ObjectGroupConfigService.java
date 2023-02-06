/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.service;


import com.elcom.metacen.data.process.model.ObjectGroupConfig;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigRequestDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigResponseDTO;
import org.springframework.data.domain.Page;


/**
 * @author Admin
 */
public interface ObjectGroupConfigService {

    ObjectGroupConfig save(ObjectGroupConfigRequestDTO objectGroupConfigRequestDTO, String createBy);

    ObjectGroupConfig findByUuid(String uuid);

    ObjectGroupConfig update(ObjectGroupConfig objectGroupConfig, ObjectGroupConfigRequestDTO objectGroupConfigRequestDTO, String modifiedBy);

    Page<ObjectGroupConfigResponseDTO> findListObjectGroupConfig(ObjectGroupConfigFilterDTO objectGroupConfigFilterDTO);

    ObjectGroupConfig delete(ObjectGroupConfig objectGroupConfig);

    ObjectGroupConfig statusChange(ObjectGroupConfig objectGroupConfig, Integer isActive);

    ObjectGroupConfig findByName(String name);
}
