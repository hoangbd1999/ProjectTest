/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.service;


import com.elcom.metacen.data.process.model.ObjectGroupConfig;
import com.elcom.metacen.data.process.model.ObjectGroupGeneralConfig;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigRequestDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigResponseDTO;
import org.springframework.data.domain.Page;


/**
 * @author Admin
 */
public interface ObjectGroupGeneralConfigService {

    void update(int togetherTime,int distanceLevel,String modifiedBy);

    ObjectGroupGeneralConfig findByTogetherTime();

}
