package com.elcom.metacen.enrich.data.validation;

import com.elcom.metacen.enrich.data.model.dto.SatelliteImageDataAnalyzedFilterDTO;
import com.elcom.metacen.utils.DateUtil;
import com.elcom.metacen.utils.StringUtil;

public class SatelliteImageDataValidation extends AbstractValidation {

    public String validateSatelliteImageDataFilter(SatelliteImageDataAnalyzedFilterDTO req) {
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
