package com.elcom.metacen.enrich.data.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailContentDTO {

    private String mailFrom;
    private String mailTo;
    private String mailCc;
    private String mailBcc;
    private long time;
    private String subject;
    private String plainText;
    private String htmlText;
    private List<String> attachmentFiles;
    private List<String> attachmentImages;
}
