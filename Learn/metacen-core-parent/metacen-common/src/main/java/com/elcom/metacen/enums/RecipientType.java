/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Admin
 */
public enum RecipientType {
    CC("Cc", "Cc"),
    TO("To", "To"),
    BCC("Bcc", "Bcc"),
    NULL("null", "null");
    private String code;
    private String description;

    RecipientType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Set<String> getListFmsDeviceType() {
        return new HashSet<>(Arrays.asList(
                CC.code,
                TO.code,
                BCC.code
        ));
    }
    public static RecipientType of(String type){
        switch (type) {
            case "Cc":
                return RecipientType.CC;
            case "To":
                return RecipientType.TO;
            case "Bcc":
                return RecipientType.BCC;
            default: return RecipientType.NULL;
        }
    }
}
