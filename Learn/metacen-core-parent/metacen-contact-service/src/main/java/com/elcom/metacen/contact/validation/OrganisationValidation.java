package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.OrganisationRequestDTO;
import com.elcom.metacen.contact.model.dto.OrganisationFilterDTO;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganisationValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganisationValidation.class);

    public String validateFilterOrganisation(OrganisationFilterDTO organisationFilterDTO) {
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateOrganisationRequest(OrganisationRequestDTO organisationDTO) {
        if (StringUtil.isNullOrEmpty(organisationDTO.getName())) {
            getMessageDes().add("Tên không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
