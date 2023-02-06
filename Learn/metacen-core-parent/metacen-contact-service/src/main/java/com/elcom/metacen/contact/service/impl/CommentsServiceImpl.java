package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.contact.model.Comments;
import com.elcom.metacen.contact.model.dto.CommentsDTO;
import com.elcom.metacen.contact.repository.CommentsRepository;
import com.elcom.metacen.contact.service.CommentsService;
import com.elcom.metacen.utils.StringUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

/**
 * @author hoangbd
 */
@Service
public class CommentsServiceImpl implements CommentsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypesServiceImpl.class);

    @Autowired
    CommentsRepository commentsRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public Comments save(CommentsDTO commentsDTO, String createUser) {
        try {
            Comments comments = modelMapper.map(commentsDTO, Comments.class);
            comments.setId(null);
            comments.setCreatedTime(new Date());
            comments.setUpdatedTime(new Date());
            comments.setCreatedUser(createUser);
            comments.setIsDeleted(DataDeleteStatus.NOT_DELETED.code());

            Comments response = commentsRepository.save(comments);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Save unit failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Comments findById(Long id) {
        Optional<Comments> comments = commentsRepository.findByIdAndIsDeleted(id,DataDeleteStatus.NOT_DELETED.code());
        if (comments.isPresent()) {
            return comments.get();
        }
        return null;
    }

    @Override
    public Comments updateComment(Comments comments, CommentsDTO commentsDTO, String updateUser) {
        try {
            comments.setType(commentsDTO.getType());
            comments.setRefId(commentsDTO.getRefId());
            comments.setContent(commentsDTO.getContent());
            comments.setContentUnsigned(commentsDTO.getContentUnsigned());
            comments.setUpdatedTime(new Date());
            comments.setUpdatedUser(updateUser);
            Comments response = commentsRepository.save(comments);
            return response;
        } catch (Exception ex) {
            LOGGER.error("Update people failed >>> {}", ex.toString());
            return null;
        }
    }

    @Override
    public Page<Comments> findListComment(Integer currentPage, Integer rowsPerPage) {
        Sort sort = Sort.by("id").ascending();
        return commentsRepository.findAllByIsDeleted(PageRequest.of(currentPage, rowsPerPage,sort),DataDeleteStatus.NOT_DELETED.code());
    }

    @Override
    public int delete(Long id) {
        return commentsRepository.delete(id);
    }
}
