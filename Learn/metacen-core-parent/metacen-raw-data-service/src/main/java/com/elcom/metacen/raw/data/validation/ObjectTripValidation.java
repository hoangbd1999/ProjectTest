package com.elcom.metacen.raw.data.validation;

import com.elcom.metacen.raw.data.model.dto.ObjectTripRequest;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTripValidation extends AbstractValidation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTripValidation.class);

    public String validateObjectTripRequest(ObjectTripRequest objectTripRequest) {
        if (StringUtil.isNullOrEmpty(objectTripRequest.getFromTime())) {
            getMessageDes().add("FromTime không được để trống");
        }
        if (StringUtil.isNullOrEmpty(objectTripRequest.getToTime())) {
            getMessageDes().add("ToTime không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
