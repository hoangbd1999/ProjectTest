/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.comment.service.impl;

import com.elcom.metacen.comment.model.Comment;
import com.elcom.metacen.comment.model.dto.CommentFilterDTO;
import com.elcom.metacen.comment.model.dto.CommentRequestDTO;
import com.elcom.metacen.comment.repository.CommentRepository;
import com.elcom.metacen.comment.service.CommentService;
import com.elcom.metacen.enums.DataDeleteStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author Admin
 */
@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    CommentRepository commentRepository;


    @Override
    public Comment save(CommentRequestDTO commentRequestDTO) {
        return commentRepository.insert(commentRequestDTO);
    }

    @Override
    public Comment findByUuid(String uuid) {
        try {
            return commentRepository.findByUuidAndIsDeleted(uuid, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("Find by uuid failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public Comment update(Comment comment, CommentRequestDTO commentRequestDTO) {
        try {
            return commentRepository.update(comment, commentRequestDTO);
        } catch (Exception e) {
            LOGGER.error("Find by uuid failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public Comment delete(int isDeleted, String uuid) {
        return commentRepository.delete(isDeleted, uuid);
    }

    @Override
    public Page<Comment> findByRefIdAndType(CommentFilterDTO commentFilterDTO) {
        try {
            return commentRepository.findByRefIdAndIsDeleted(commentFilterDTO, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("Find by refId failed >>> {}", e.toString());
            return null;
        }
    }

    @Override
    public List<Comment> findByRefId(String refId) {
        try {
            return commentRepository.findByRefId(refId, DataDeleteStatus.NOT_DELETED.code());
        } catch (Exception e) {
            LOGGER.error("Find by refId failed >>> {}", e.toString());
            return null;
        }
    }
}
