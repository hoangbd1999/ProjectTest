package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.AreasDTO.AreasDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasFilterDTO;
import com.elcom.metacen.contact.model.dto.AreasDTO.AreasRequestDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AreasValidation extends AbstractValidation {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationValidation.class);

    public String validateFilterAreas(AreasFilterDTO areasFilterDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateAreas(AreasRequestDTO areasRequestDTO) {
        if (StringUtil.isNullOrEmpty(areasRequestDTO.getName())) {
            getMessageDes().add("Tên không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
