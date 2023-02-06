package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.PeopleRequestDTO;
import com.elcom.metacen.contact.model.dto.PeopleFilterDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeopleValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeopleValidation.class);
    
    public String validatePeople(PeopleRequestDTO peopleRequestDTO) {
        if (StringUtil.isNullOrEmpty(peopleRequestDTO.getName())) {
            getMessageDes().add("Tên không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterPeople(PeopleFilterDTO peopleFilterDTO ) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
