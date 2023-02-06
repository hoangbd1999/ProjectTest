/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.ObjectGroupDefine;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineRequestDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineResponseDTO;
import org.springframework.data.domain.Page;



/**
 * @author Admin
 */
public interface ObjectGroupDefineService {

    Page<ObjectGroupDefineResponseDTO> findListObjectGroupDefine(ObjectGroupDefineFilterDTO objectGroupDefineFilterDTO);

    ObjectGroupDefine findByUuid(String uuid);

    ObjectGroupDefine findByName(String name);

    ObjectGroupDefine save(ObjectGroupDefineRequestDTO objectGroupDefineRequestDTO, String createBy);

    ObjectGroupDefine delete(ObjectGroupDefine objectGroupDefine);

    ObjectGroupDefine statusChange(ObjectGroupDefine objectGroupDefine, Boolean isMainObject, String objectUuid);

    ObjectGroupDefine update(ObjectGroupDefine objectGroupDefine, ObjectGroupDefineRequestDTO objectGroupDefineRequestDTO, String modifiedBy);

}
