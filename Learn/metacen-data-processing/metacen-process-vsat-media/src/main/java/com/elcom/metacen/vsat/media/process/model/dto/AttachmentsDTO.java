package com.elcom.metacen.vsat.media.process.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class AttachmentsDTO {

    private String name;
    private String url;
    private List<TextDTO> contents;
    private Boolean status;
}
