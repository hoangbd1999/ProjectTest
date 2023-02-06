package com.elcom.metacen.data.process.repository;

import com.elcom.metacen.data.process.model.DataProcessConfig;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.text.ParseException;

/**
 *
 * @author Admin
 */
public interface CustomDataProcessConfigRepository extends BaseCustomRepository<DataProcessConfig> {

    Page<DataProcessConfigResponseDTO> search(DataProcessConfigFilterDTO dataProcessConfigFilterDTO, Pageable pageable) throws ParseException;

}
