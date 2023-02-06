package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.OtherObject;
import com.elcom.metacen.contact.model.dto.OtherObjectFilterDTO;
import com.elcom.metacen.contact.model.dto.OtherObjectResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 *
 * @author Admin
 */
public interface CustomOtherObjectRepository extends BaseCustomRepository<OtherObject> {

    Page<OtherObjectResponseDTO> search(OtherObjectFilterDTO otherObjectFilterDTO, Pageable pageable);

    OtherObjectResponseDTO findOtherObjectByUuid(String uuid);


}
