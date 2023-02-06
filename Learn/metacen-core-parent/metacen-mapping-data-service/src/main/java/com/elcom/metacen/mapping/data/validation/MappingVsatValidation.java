package com.elcom.metacen.mapping.data.validation;

import com.elcom.metacen.mapping.data.model.dto.MappingVsatFilterDTO;
import com.elcom.metacen.mapping.data.model.dto.MappingVsatRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingVsatValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingVsatValidation.class);
    
    public String validateMappingVsat(MappingVsatRequestDTO mappingVsatRequestDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterMappingVsat(MappingVsatFilterDTO mappingVsatFilterDTO ) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
