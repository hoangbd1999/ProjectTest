/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.data.process.service;

import com.elcom.metacen.data.process.model.DataProcessConfig;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigRequestDTO;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author Admin
 */
public interface DataProcessConfigService {

    DataProcessConfig save(DataProcessConfigRequestDTO dataProcessConfigRequestDTO, String createBy);

    DataProcessConfig findByUuid(String uuid);

    DataProcessConfig update(DataProcessConfig dataProcessConfig, DataProcessConfigRequestDTO dataProcessConfigRequestDTO, String modifiedBy);

    Page<DataProcessConfigResponseDTO> findListDataProcessConfig(DataProcessConfigFilterDTO dataProcessConfigFilterDTO);

    List<DataProcessConfig> getList();

    DataProcessConfig delete(DataProcessConfig dataProcessConfig);

    DataProcessConfig statusChange(DataProcessConfig dataProcessConfig, Integer status);
}
