package com.elcom.metacen.vsat.media.process.model.dto;

import com.elcom.metacen.enums.RecipientType;
import lombok.Data;

@Data
public class RecipientDTO {

    private String name;
    private String address;
    private RecipientType type;
}
