package com.elcom.metacen.mapping.data.validation;

import com.elcom.metacen.mapping.data.model.dto.MappingAisRequestDTO;
import com.elcom.metacen.mapping.data.model.dto.MappingAisFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingAisValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(MappingAisValidation.class);

    public String validateMappingAis(MappingAisRequestDTO mappingAisRequestDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterMappingAis(MappingAisFilterDTO mappingAisFilterDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
