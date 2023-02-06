package com.elcom.metacen.raw.data.validation;

import com.elcom.metacen.raw.data.model.dto.SatelliteImageDataFilterDTO;
import com.elcom.metacen.utils.DateUtil;
import com.elcom.metacen.utils.StringUtil;

public class SatelliteImageDataValidation extends AbstractValidation {

    public String validateSatelliteImageDataFilter(SatelliteImageDataFilterDTO req) {
        String timeFormat = "yyyy-MM-dd HH:mm:ss";

        if (StringUtil.isNullOrEmpty(req.getFromTime())) {
            getMessageDes().add("FromTime không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getFromTime(), timeFormat)) {
            getMessageDes().add("FromTime không đúng định dạng [" + timeFormat + "]");
        }

        if (StringUtil.isNullOrEmpty(req.getToTime())) {
            getMessageDes().add("ToTime không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getToTime(), timeFormat)) {
            getMessageDes().add("ToTime không đúng định dạng [" + timeFormat + "]");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
