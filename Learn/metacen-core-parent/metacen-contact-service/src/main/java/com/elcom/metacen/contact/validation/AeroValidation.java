package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.AeroDTO.AeroFilterDTO;
import com.elcom.metacen.contact.model.dto.AeroDTO.AeroInsertRequestDTO;
import com.elcom.metacen.contact.model.dto.OrganisationFilterDTO;
import com.elcom.metacen.contact.model.dto.OrganisationRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.codehaus.stax2.validation.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AeroValidation extends AbstractValidation {
    private static final Logger LOGGER = LoggerFactory.getLogger(AeroValidation.class);

    public String validateFilterAero(AeroFilterDTO aeroFilterDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateAero(AeroInsertRequestDTO aeroInsertRequestDTO) {
        if (StringUtil.isNullOrEmpty(aeroInsertRequestDTO.getName())) {
            getMessageDes().add("Tên không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
