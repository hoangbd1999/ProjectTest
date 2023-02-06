package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.GroupDTO.GroupDTO;
import com.elcom.metacen.contact.model.dto.GroupDTO.GroupFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupValidation.class);
    
    public String validateGroup(GroupDTO groupDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterGroup(GroupFilterDTO groupFilterDTO ) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
