package com.elcom.metacen.data.process.validation;

import com.elcom.metacen.data.process.model.dto.DataProcessConfigFilterDTO;
import com.elcom.metacen.data.process.model.dto.DataProcessConfigRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataProcessConfigValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessConfigValidation.class);
    
    public String validateDataProcessConfig(DataProcessConfigRequestDTO dataProcessConfigRequestDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterDataProcessConfig(DataProcessConfigFilterDTO dataProcessConfigFilterDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
