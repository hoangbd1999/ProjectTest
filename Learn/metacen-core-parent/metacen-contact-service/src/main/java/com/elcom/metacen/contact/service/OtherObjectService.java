/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.OtherObject;
import com.elcom.metacen.contact.model.dto.*;
import org.springframework.data.domain.Page;

/**
 * @author hoangbd
 */
public interface OtherObjectService {

    OtherObject save(OtherObjectRequestDTO otherObjectRequestDTO, String createBy);

    OtherObject findByUuid(String uuid);

    OtherObject updateOtherObject(OtherObject otherObject, OtherObjectRequestDTO otherObjectRequestDTO, String modifiedBy);

    Page<OtherObjectResponseDTO> findListOtherObject(OtherObjectFilterDTO otherObjectFilterDTO);

    OtherObject delete(OtherObject otherObject, String modifiedBy);

    OtherObjectResponseDTO findOtherObjectByUuid(String uuid);

}
