package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentsDetailDTO implements Serializable {
    private Long id;
    private Integer type;
    private String refId;
    private String content;
    private String contentUnsigned;
    private String createdUser;
    private String updatedUser;
    private Date createdTime;
    private Date updatedTime;
}
