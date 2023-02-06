package com.elcom.metacen.content.dto;

import com.elcom.metacen.content.enums.RecipientType;
import lombok.Data;
import org.simplejavamail.api.email.Recipient;

import java.util.ArrayList;
import java.util.List;

public class EmailDTO {
    RecipientDTO from;
    RecipientDTO replyTo;
    List<RecipientDTO> to;
    List<AttachmentsDTO> attachments;
    String contents;
    String subject;
    String scanVirus;
    String scanResult;
    String userAgent;
    String contentLanguage;
    String XMail;
    Object raw ;

    public Object getRaw() {
        return raw;
    }

    public void setRaw(Object raw) {
        this.raw = raw;
    }

    public String getXMail() {
        return XMail;
    }

    public void setXMail(String XMail) {
        this.XMail = XMail;
    }

    public void setFrom(RecipientDTO from) {
        this.from = from;
    }

    public void setReplyTo(RecipientDTO replyTo) {
        this.replyTo = replyTo;
    }

    public String getScanVirus() {
        return scanVirus;
    }

    public void setScanVirus(String scanVirus) {
        this.scanVirus = scanVirus;
    }

    public String getScanResult() {
        return scanResult;
    }

    public void setScanResult(String scanResult) {
        this.scanResult = scanResult;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public void setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    public RecipientDTO getFrom() {
        return from;
    }

    public void setFrom(Recipient from) {
        RecipientDTO recipientDTO = new RecipientDTO();
        recipientDTO.setAddress(from.getAddress());
        recipientDTO.setName(from.getName());
        if(from.getType()!=null)
            recipientDTO.setType(RecipientType.of(from.getType().toString()));
        this.from = recipientDTO;
    }

    public RecipientDTO getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Recipient replyTo) {
        RecipientDTO recipientDTO = new RecipientDTO();
        recipientDTO.setAddress(replyTo.getAddress());
        recipientDTO.setName(replyTo.getName());
        if(replyTo.getType()!=null)
            recipientDTO.setType(RecipientType.of(replyTo.getType().toString()));
        this.replyTo = recipientDTO;
    }

    public List<RecipientDTO> getTo() {
        return to;
    }

    public void setTo(List<Recipient> to) {
        List<RecipientDTO> result = new ArrayList<>();
        for (Recipient data: to
             ) {
            RecipientDTO recipientDTO = new RecipientDTO();
            recipientDTO.setAddress(data.getAddress());
            recipientDTO.setName(data.getName());
            if(data.getType()!=null)
                recipientDTO.setType(RecipientType.of(data.getType().toString()));
            result.add(recipientDTO);
        }
        this.to = result;
    }

    public List<AttachmentsDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentsDTO> attachments) {
        this.attachments = attachments;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
