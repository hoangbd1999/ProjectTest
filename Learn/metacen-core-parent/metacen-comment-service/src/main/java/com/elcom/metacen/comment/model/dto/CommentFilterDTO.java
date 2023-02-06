package com.elcom.metacen.comment.model.dto;

import lombok.*;

import java.util.List;

/**
 *
 * @author Admin
 */
@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentFilterDTO {
    private Integer page;
    private Integer size;
    private String refId;
    private Integer type;
}
