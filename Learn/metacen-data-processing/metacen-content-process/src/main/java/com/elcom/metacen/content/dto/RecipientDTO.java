package com.elcom.metacen.content.dto;

import com.elcom.metacen.content.enums.RecipientType;
import lombok.Data;

import javax.mail.Message;

@Data
public class RecipientDTO {
    private String name;
    private String address;
    private RecipientType type;
}
