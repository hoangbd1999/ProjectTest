package com.elcom.metacen.enrich.data.model.dto;

import lombok.*;

/**
 *
 * @author Admin
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {

    private String uuidKey;
    private Integer type;
    private String refId;
    private String content;
    private String contentUnsigned;
    private String createdUser;
    private String updatedUser;
    private String createdTime;
    private String updatedTime;
    private String ingestTime;
    private Integer isDeleted;

}
