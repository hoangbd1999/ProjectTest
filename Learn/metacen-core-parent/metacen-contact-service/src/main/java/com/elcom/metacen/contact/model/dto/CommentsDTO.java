package com.elcom.metacen.contact.model.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

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
public class CommentsDTO implements Serializable {
    private Integer type;
    private String refId;
    private String content;
    private String contentUnsigned;
    private String createdUser;
    private String updatedUser;
}
