package com.elcom.metacen.raw.data.validation;

import com.elcom.metacen.raw.data.model.dto.SatelliteDataFilterDTO;
import com.elcom.metacen.utils.DateUtil;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SatelliteDataValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(SatelliteDataValidation.class);

    public String validateSatelliteDataFilter(SatelliteDataFilterDTO req) {
        String timeFormat = "yyyy-MM-dd HH:mm:ss";

        if (StringUtil.isNullOrEmpty(req.getFromTime())) {
            getMessageDes().add("Từ ngày không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getFromTime(), timeFormat)) {
            getMessageDes().add("Từ ngày không đúng định dạng [" + timeFormat + "]");
        }

        if (StringUtil.isNullOrEmpty(req.getToTime())) {
            getMessageDes().add("Đến ngày không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getToTime(), timeFormat)) {
            getMessageDes().add("Đến ngày không đúng định dạng [" + timeFormat + "]");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
