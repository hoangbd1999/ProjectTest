package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.KeywordFilterDTO;
import com.elcom.metacen.contact.model.dto.SideDTO;
import com.elcom.metacen.contact.model.dto.SideFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SideValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(SideValidation.class);
    
    public String validateSide(SideDTO sideDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterSide(SideFilterDTO sideFilterDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
