package com.elcom.metacen.comment.model.dto;

import lombok.*;
import java.util.Date;

/**
 *
 * @author Admin
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {

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
