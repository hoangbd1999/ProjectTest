package com.elcom.metacen.enrich.data.validation;

import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeFilterDTO;
import com.elcom.metacen.enrich.data.model.dto.SatelliteImageChangeRequestDTO;
import com.elcom.metacen.utils.DateUtil;
import com.elcom.metacen.utils.StringUtil;

public class SatelliteImageChangeValidation extends AbstractValidation {

    public String ValidationSatelliteImageChange(SatelliteImageChangeRequestDTO req) {
        String timeFormat = "yyyy-MM-dd HH:mm:ss";

        if (StringUtil.isNullOrEmpty(req.getTimeFileOrigin())) {
            getMessageDes().add("Từ ngày không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getTimeFileOrigin(), timeFormat)) {
            getMessageDes().add("Từ ngày không đúng định dạng [" + timeFormat + "]");
        }

        if (StringUtil.isNullOrEmpty(req.getTimeFileCompare())) {
            getMessageDes().add("Đến ngày không được để trống");
        } else if (!DateUtil.isValidTimeByFormat(req.getTimeFileCompare(), timeFormat)) {
            getMessageDes().add("Đến ngày không đúng định dạng [" + timeFormat + "]");
        }

        if(!StringUtil.isNullOrEmpty(req.getTimeFileOrigin()) && !StringUtil.isNullOrEmpty(req.getTimeFileCompare())){
            if(req.getTimeFileOrigin().equals(req.getTimeFileCompare())){
                getMessageDes().add("Thời gian không được trùng nhau");
            }
        }
        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateSatelliteImageChangeFilter(SatelliteImageChangeFilterDTO req) {
        return null;
    }
}
