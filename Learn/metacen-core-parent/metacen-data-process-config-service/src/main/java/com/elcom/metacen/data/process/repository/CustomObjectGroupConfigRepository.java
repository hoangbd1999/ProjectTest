package com.elcom.metacen.data.process.repository;

import com.elcom.metacen.data.process.model.ObjectGroupConfig;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.ObjectGroupConfigDTO.ObjectGroupConfigResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;

/**
 *
 * @author Admin
 */
public interface CustomObjectGroupConfigRepository extends BaseCustomRepository<ObjectGroupConfig> {

    Page<ObjectGroupConfigResponseDTO> search(ObjectGroupConfigFilterDTO objectGroupConfigFilterDTO, Pageable pageable) throws ParseException;

}
