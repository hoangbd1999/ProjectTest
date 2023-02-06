package com.elcom.metacen.contact.service;


import com.elcom.metacen.contact.model.Comments;
import com.elcom.metacen.contact.model.dto.CommentsDTO;
import org.springframework.data.domain.Page;

/**
 * @author hoangbd
 */
public interface CommentsService {

    Comments save(CommentsDTO commentsDTO, String createUser);

    Comments findById(Long id);

    Comments updateComment(Comments comments, CommentsDTO commentsDTO, String updateUser);

    Page<Comments> findListComment(Integer currentPage, Integer rowsPerPage);

    int delete(Long id);
}
