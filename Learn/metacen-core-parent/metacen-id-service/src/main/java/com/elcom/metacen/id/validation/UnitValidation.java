package com.elcom.metacen.id.validation;

import com.elcom.metacen.id.constant.Constant;
import com.elcom.metacen.id.model.Unit;
import com.elcom.metacen.id.model.User;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitValidation extends AbstractValidation{
    private static final Logger LOGGER = LoggerFactory.getLogger(UserValidation.class);

    public String validateInsertUnit(Unit unit) {
        if (unit == null) {
            return "PayLoad không hợp lệ";
        }
        if (StringUtil.isNullOrEmpty(unit.getEmail())
                && StringUtil.isNullOrEmpty(unit.getPhone())) {
            getMessageDes().add("Email hoặc số điện thoại không được để trống");
        }

        if (!StringUtil.isNullOrEmpty(unit.getEmail()) && !StringUtil.validateEmail(unit.getEmail())) {
            getMessageDes().add("Email không đúng định dạng");
        }

        if (!StringUtil.isNullOrEmpty(unit.getPhone()) && !StringUtil.checkMobilePhoneNumberNew(unit.getPhone())) {
            getMessageDes().add("Số điện thoại không đúng định dạng");
        }

        if (StringUtil.isNullOrEmpty(unit.getName())) {
            getMessageDes().add("Tên đơn vị không được để trống");
        }

        if (StringUtil.isNullOrEmpty(unit.getCode())) {
            getMessageDes().add("Mã đơn vị không được để trống");
        }

        if (StringUtil.isNullOrEmpty(unit.getAddress())) {
            getMessageDes().add("Địa chỉ đơn vị không được để trống");
        }

        if (StringUtil.isNullOrEmpty(unit.getLisOfStage())) {
            getMessageDes().add("danh sách đoạn đường không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }

    public String validateUpdateUnit(Unit unit) {
        if (unit == null || unit.getUuid() == null) {
            return "PayLoad không hợp lệ";
        }

        if (StringUtil.isNullOrEmpty(unit.getEmail())
                && StringUtil.isNullOrEmpty(unit.getPhone())) {
            getMessageDes().add("Email hoặc số điện thoại không được để trống");
        }

        if (!StringUtil.isNullOrEmpty(unit.getEmail()) && !StringUtil.validateEmail(unit.getEmail())) {
            getMessageDes().add("Email không đúng định dạng");
        }

        if (!StringUtil.isNullOrEmpty(unit.getPhone()) && !StringUtil.checkMobilePhoneNumberNew(unit.getPhone())) {
            getMessageDes().add("Số điện thoại không đúng định dạng");
        }

        if (StringUtil.isNullOrEmpty(unit.getName())) {
            getMessageDes().add("Tên đơn vị không được để trống");
        }

        if (StringUtil.isNullOrEmpty(unit.getCode())) {
            getMessageDes().add("Mã đơn vị không được để trống");
        }

        if (StringUtil.isNullOrEmpty(unit.getAddress())) {
            getMessageDes().add("Địa chỉ đơn vị không được để trống");
        }

        if (StringUtil.isNullOrEmpty(unit.getLisOfStage())) {
            getMessageDes().add("danh sách đoạn đường không được để trống");
        }

        return !isValid() ? this.buildValidationMessage() : null;
    }
}
