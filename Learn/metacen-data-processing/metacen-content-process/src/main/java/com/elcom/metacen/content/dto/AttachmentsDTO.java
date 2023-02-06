package com.elcom.metacen.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class AttachmentsDTO {
    String name;
    String url;
    List<TextDTO> contents;
    Boolean status;
}
