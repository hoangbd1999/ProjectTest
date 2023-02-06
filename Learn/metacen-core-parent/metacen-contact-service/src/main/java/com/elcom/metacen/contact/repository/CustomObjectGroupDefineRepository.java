package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectGroupDefine;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineFilterDTO;
import com.elcom.metacen.contact.model.dto.ObjectGroupDefine.ObjectGroupDefineResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 *
 * @author Admin
 */
public interface CustomObjectGroupDefineRepository extends BaseCustomRepository<ObjectGroupDefine> {

    Page<ObjectGroupDefineResponseDTO> search(ObjectGroupDefineFilterDTO objectGroupDefineFilterDTO, Pageable pageable);

}
