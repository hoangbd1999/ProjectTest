package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.ObjectCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectValidation.class);

    public String validateObjectFilter(ObjectCriteria objectCriteria) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
