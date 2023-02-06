package com.elcom.metacen.comment.business;

import com.elcom.metacen.comment.model.Comment;
import com.elcom.metacen.comment.validation.CommentValidation;
import com.elcom.metacen.enums.DataDeleteStatus;
import com.elcom.metacen.enums.DataNoteStatus;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.comment.constant.Constant;
import com.elcom.metacen.comment.model.dto.*;
import com.elcom.metacen.comment.service.CommentService;

import com.elcom.metacen.utils.StringUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Controller
public class CommentBusiness extends BaseBusiness {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommentBusiness.class);

    @Autowired
    private CommentService commentService;

    @Autowired
    ModelMapper modelMapper;

    public ResponseMessage insert(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) throws ParseException {
        LOGGER.info("Create comment with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }

            CommentRequestDTO commentRequestDTO = buildCommentDTO(bodyParam, dto.getUserName());
            String validationMsg = new CommentValidation().ValidationComment(commentRequestDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            this.commentService.save(commentRequestDTO);
            bodyParam.put("uuid", commentRequestDTO.getRefId());
            bodyParam.put("isNoted", DataNoteStatus.NOTED.code());
            this.noteChange(bodyParam);
            if (commentRequestDTO != null) {
                return new ResponseMessage(new MessageContent(commentRequestDTO));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage update(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            try {
                Comment comment = commentService.findByUuid(pathParam);
                if (comment == null) {
                    return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại",
                            new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
                }
                CommentRequestDTO commentRequestDTO = buildUpdateCommentDTO(comment, bodyParam, dto.getUserName());
                String validationMsg = new CommentValidation().ValidationComment(commentRequestDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Comment result = commentService.update(comment, commentRequestDTO);
                if (result != null) {
                    return new ResponseMessage(new MessageContent(commentRequestDTO));
                } else {
                    return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                            new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
                }
            } catch (Exception e) {
                LOGGER.error("Update failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện tác vụ này", null));
        }
    }

    public ResponseMessage delete(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            Comment comment = commentService.findByUuid(pathParam);
            if (comment == null) {
                return new ResponseMessage(HttpStatus.OK.value(), "Dữ liệu không tồn tại",
                        new MessageContent(HttpStatus.OK.value(), "Dữ liệu không tồn tại", null));
            }
            Map<String, Object> bodyParam = new HashMap<>();
            bodyParam.put("uuid", comment.getRefId());
            bodyParam.put("isNoted", DataNoteStatus.NO_NOTED.code());
            this.commentService.delete(DataDeleteStatus.DELETED.code(), pathParam);
            List<Comment> listComment = commentService.findByRefId(comment.getRefId());
            if (!listComment.isEmpty()) {
                this.noteChange(bodyParam);
            }
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getComment(String requestPath, Map<String, String> headerParam, String urlParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            if (urlParam == null || urlParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            CommentFilterDTO commentFilterDTO = buildCommentFilterRequest(urlParam);
            Page<Comment> pagedResult = commentService.findByRefIdAndType(commentFilterDTO);
            if (pagedResult == null) {
                return new ResponseMessage(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, new MessageContent(HttpStatus.OK.value(), Constant.VALIDATION_DATA_NOT_FOUND, null));
            }
            return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }


    private CommentRequestDTO buildCommentDTO(Map<String, Object> bodyParam, String createdUser) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String uuidKey = UUID.randomUUID().toString();
            Integer type = bodyParam.get("type") != null ? (Integer) bodyParam.get("type") : 0;
            String refId = (String) bodyParam.getOrDefault("refId", "");
            String content = (String) bodyParam.getOrDefault("content", "");
            String contentUnsigned = (String) bodyParam.getOrDefault("contentUnsigned", "");
            String newDate = dateFormat.format(new Date());


            CommentRequestDTO commentRequestDTO = CommentRequestDTO.builder()
                    .uuidKey(uuidKey)
                    .type(type)
                    .refId(refId)
                    .content(content)
                    .contentUnsigned(contentUnsigned)
                    .createdUser(createdUser)
                    .updatedUser(createdUser)
                    .createdTime(newDate)
                    .updatedTime(newDate)
                    .ingestTime(newDate)
                    .isDeleted(DataDeleteStatus.NOT_DELETED.code())
                    .build();

            return commentRequestDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    private CommentRequestDTO buildUpdateCommentDTO(Comment comment, Map<String, Object> bodyParam, String createdUser) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String uuidKey = comment.getUuidKey();
            Integer type = bodyParam.get("type") != null ? (Integer) bodyParam.get("type") : 0;
            String refId = (String) bodyParam.getOrDefault("refId", "");
            String content = (String) bodyParam.getOrDefault("content", "");
            String contentUnsigned = (String) bodyParam.getOrDefault("contentUnsigned", "");
            String newDate = dateFormat.format(new Date());
            String createdTime = dateFormat.format(comment.getCreatedTime());
            String ingestTime = dateFormat.format(comment.getIngestTime());


            CommentRequestDTO commentRequestDTO = CommentRequestDTO.builder()
                    .uuidKey(uuidKey)
                    .type(type)
                    .refId(refId)
                    .content(content)
                    .contentUnsigned(contentUnsigned)
                    .createdUser(comment.getCreatedUser())
                    .updatedUser(createdUser)
                    .createdTime(createdTime)
                    .updatedTime(newDate)
                    .ingestTime(ingestTime)
                    .isDeleted(DataDeleteStatus.NOT_DELETED.code())
                    .build();

            return commentRequestDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    private CommentFilterDTO buildCommentFilterRequest(String urlParam) {
        try {
            Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
            Integer page = params.get("page") != null ? Integer.parseInt(params.get("page")) : 0;
            Integer size = params.get("size") != null ? Integer.parseInt(params.get("size")) : 20;
            String refId = params.get("refId");
            Integer type = params.get("type") != null ? Integer.parseInt(params.get("type")) : 0;

            CommentFilterDTO commentFilterDTO = CommentFilterDTO.builder()
                    .page(page)
                    .size(size)
                    .refId(refId)
                    .type(type)
                    .build();

            return commentFilterDTO;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }

        return null;
    }
}
