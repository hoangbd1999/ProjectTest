package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.InfrastructureFilterDTO;
import com.elcom.metacen.contact.model.dto.InfrastructureRequestDTO;
import com.elcom.metacen.contact.model.dto.PeopleFilterDTO;
import com.elcom.metacen.contact.model.dto.PeopleRequestDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfrastructureValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureValidation.class);
    
    public String validateInfrastructure(InfrastructureRequestDTO infrastructureRequestDTO) {
        if (StringUtil.isNullOrEmpty(infrastructureRequestDTO.getName())) {
            getMessageDes().add("Tên không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateFilterInfrastructure(InfrastructureFilterDTO infrastructureFilterDTO ) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
