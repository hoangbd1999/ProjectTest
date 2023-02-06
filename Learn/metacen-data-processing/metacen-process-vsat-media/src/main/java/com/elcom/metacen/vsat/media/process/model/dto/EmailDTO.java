package com.elcom.metacen.vsat.media.process.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class EmailDTO {

    private RecipientDTO from;
    private RecipientDTO replyTo;
    private List<RecipientDTO> to;
    private List<AttachmentsDTO> attachments;
    private String contents;
    private String subject;
    private String scanVirus;
    private String scanResult;
    private String userAgent;
    private String contentLanguage;
    private String XMail;
    private Object raw;
}
