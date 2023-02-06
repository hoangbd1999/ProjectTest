/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.comment.service;

import com.elcom.metacen.comment.model.Comment;
import com.elcom.metacen.comment.model.dto.CommentFilterDTO;
import com.elcom.metacen.comment.model.dto.CommentRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 *
 * @author Admin
 */
public interface CommentService {

    Comment save (CommentRequestDTO commentRequestDTO);

    Comment findByUuid(String uuid);

    Comment update(Comment comment, CommentRequestDTO commentRequestDTO);

    Comment delete(int isDeleted, String uuid);

    Page<Comment> findByRefIdAndType(CommentFilterDTO commentFilterDTO);

    List<Comment> findByRefId(String refId);
}
