package com.elcom.metacen.raw.data.utils;

import com.elcom.metacen.raw.data.config.ApplicationConfig;
import com.elcom.metacen.raw.data.model.dto.MailContentDTO;
import com.elcom.metacen.utils.StringUtil;
import com.google.common.io.Files;
import jakarta.mail.Message;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.simplejavamail.api.email.AttachmentResource;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.converter.EmailConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailUtil.class);

    public static MailContentDTO emlToEntity(String emlFilePath) {
        try {
            File emlFile = new File(emlFilePath);
            if (!emlFile.exists()) {
                LOGGER.error("emlFilePath [{}] is not exists", emlFilePath);
                return null;
            }

            Email email = EmailConverter.emlToEmail(emlFile);

            Recipient fromRecipient = email.getFromRecipient();
            String mailFrom = fromRecipient != null ? fromRecipient.getAddress()
                    + (!StringUtil.isNullOrEmpty(fromRecipient.getName()) ? " (" + fromRecipient.getName() + ")" : "") : "";

            String mailTo = "";
            String mailCc = "";
            String mailBcc = "";
            List<Recipient> lstRecipient = email.getRecipients();
            for (Recipient recipient : lstRecipient) {
                if (Message.RecipientType.TO.equals(recipient.getType())) {
                    mailTo += recipient.getAddress() + (!StringUtil.isNullOrEmpty(recipient.getName()) ? " (" + recipient.getName() + ")" : "") + "; ";
                } else if (Message.RecipientType.CC.equals(recipient.getType())) {
                    mailCc += recipient.getAddress() + (!StringUtil.isNullOrEmpty(recipient.getName()) ? " (" + recipient.getName() + ")" : "") + "; ";
                } else if (Message.RecipientType.BCC.equals(recipient.getType())) {
                    mailBcc += recipient.getAddress() + (!StringUtil.isNullOrEmpty(recipient.getName()) ? " (" + recipient.getName() + ")" : "") + "; ";
                }
            }

            long time = email.getSentDate() != null ? email.getSentDate().getTime() : 0;
            String subject = email.getSubject();
            String plainText = email.getPlainText();
            String htmlText = email.getHTMLText();

            List<AttachmentResource> attachments = email.getAttachments();
            List<String> lstAttachmentFiles = new ArrayList<>();
            if (!attachments.isEmpty()) {
                for (AttachmentResource attachment : attachments) {
                    String fileName = attachment.getDataSource().getName().replaceAll("[^a-zA-Z0-9.]", "");
                    String fullPath = ApplicationConfig.ROOT_FOLDER_EMAIL_FILE_ATTACHMENTS + "/files/" + System.currentTimeMillis() + "-" + fileName;
                    try {
                        Files.write(attachment.readAllBytes(), new File(fullPath));
                        lstAttachmentFiles.add(fullPath.replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, ApplicationConfig.MEDIA_LINK_ROOT_API));
                    } catch (Exception e) {
                        LOGGER.error("ex: ", e);
                    }
                }
            }

            List<AttachmentResource> embeddedImages = email.getEmbeddedImages();
            List<String> lstAttachmentImages = new ArrayList<>();
            if (!embeddedImages.isEmpty()) {
                for (AttachmentResource embeddedImage : embeddedImages) {
                    String fileName = embeddedImage.getDataSource().getName().replaceAll("[^a-zA-Z0-9.]", "");
                    String fullPath = ApplicationConfig.ROOT_FOLDER_EMAIL_FILE_ATTACHMENTS + "/images/" + System.currentTimeMillis() + "-" + fileName;
                    try {
                        Files.write(embeddedImage.readAllBytes(), new File(fullPath));
                        lstAttachmentImages.add(fullPath.replace(ApplicationConfig.ROOT_FOLDER_FILE_PATH_INTERNAL, ApplicationConfig.MEDIA_LINK_ROOT_API));
                    } catch (Exception e) {
                        LOGGER.error("ex: ", e);
                    }
                }
            }

            return new MailContentDTO(
                    mailFrom, mailTo, mailCc, mailBcc, time, subject, plainText, htmlText, lstAttachmentFiles, lstAttachmentImages
            );
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        return null;
    }
}
