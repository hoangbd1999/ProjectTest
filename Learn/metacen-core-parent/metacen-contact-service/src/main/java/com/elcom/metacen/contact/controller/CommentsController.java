package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.model.Comments;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.CommentsService;
import com.elcom.metacen.contact.validation.ContactValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;
import com.elcom.metacen.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author hoangbd
 */
@Controller
public class CommentsController extends BaseController{

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectTypesController.class);

    private static final int CURRENT_PAGE = 0;
    private static final int ROW_PER_PAGE = 20;

    @Autowired
    CommentsService commentsService;

    public ResponseMessage insertComment(String requestPath, Map<String, Object> bodyParam, Map<String, String> headerParam) {
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
            CommentsDTO commentsDTO = buildCommentsDTO(bodyParam);
            String validationMsg = new ContactValidation().validateComment(commentsDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            Comments comments = commentsService.save(commentsDTO,dto.getUserName());
            if (comments != null) {
                return new ResponseMessage(new MessageContent(transform(comments)));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updateComment(String requestPath, Map<String, Object> bodyParam ,Map<String, String> headerParam, String pathParam) {
        LOGGER.info("Update comment id {} with request >>> {}", pathParam, bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            Long id = Long.parseLong(pathParam);
            Comments comments = commentsService.findById(id);
            if (comments == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Comment không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Comment không tồn tại", null));
            }
            CommentsDTO commentsDTO = buildCommentsDTO(bodyParam);
            String validationMsg = new ContactValidation().validateComment(commentsDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Comments result = commentsService.updateComment(comments, commentsDTO,dto.getUserName());
            if (result != null) {
                return new ResponseMessage(new MessageContent(transform(result)));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getCommentById(String requestPath, Map<String, String> headerMap, String pathParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO auth = authenToken(headerMap);
        if (auth == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", auth.getUuid(), requestPath);

            if (abacStatus != null && abacStatus.getStatus()) {
                Comments comments = commentsService.findById(Long.parseLong(pathParam));
                if (comments == null) {
                    response = new ResponseMessage(new MessageContent(HttpStatus.NOT_FOUND.value(),
                            "Comment không tồn tại", null));
                } else {
                    response = new ResponseMessage(new MessageContent(comments));
                }
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem chi tiết Comment",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem chi tiết Comment ", null));
            }
        }
        return response;

    }

    public ResponseMessage getCommentList(String requestPath, Map<String, String> headerMap, String urlParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO auth = authenToken(headerMap);
        if (auth == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", auth.getUuid(), requestPath);

            if (abacStatus != null && abacStatus.getStatus()) {
                int currentPage;
                int rowsPerPage;
                if (urlParam == null || urlParam.equals("")) {
                    currentPage = CURRENT_PAGE;
                    rowsPerPage = ROW_PER_PAGE;
                } else {
                    Map<String, String> params = StringUtil.getUrlParamValues(urlParam);
                    if (Integer.parseInt(params.get("rowsPerPage")) == 0) {
                        rowsPerPage = ROW_PER_PAGE;
                        currentPage = CURRENT_PAGE;
                    } else {
                        currentPage = Integer.parseInt(params.get("currentPage"));
                        rowsPerPage = Integer.parseInt(params.get("rowsPerPage"));
                    }
                }
                String validationMsg = new ContactValidation().validateSearch(currentPage, rowsPerPage);
                if (validationMsg != null)
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));

                Page<Comments> result = this.commentsService.findListComment(currentPage, rowsPerPage);

                response = new ResponseMessage(new MessageContent(result.getContent(), result.getTotalElements()));
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem danh sách Comments",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xem danh sách Comments ", null));
            }
        }
        return response;

    }

    public ResponseMessage deleteComment(String requestPath, Map<String, String> headerMap, String pathParam) {
        ResponseMessage response = null;
        AuthorizationResponseDTO auth = authenToken(headerMap);
        if (auth == null) {
            response = new ResponseMessage(new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn chưa đăng nhập", null));
        } else {
            Map<String, Object> body = new HashMap<String, Object>();
            ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", auth.getUuid(), requestPath);

            if (abacStatus != null && abacStatus.getStatus()) {
                Comments comment = commentsService.findById(Long.parseLong(pathParam));
                if (Objects.isNull(comment)) {
                    return new ResponseMessage(new MessageContent(HttpStatus.BAD_REQUEST.value(), "Comment không tồn tại", null));
                }
                this.commentsService.delete(Long.parseLong(pathParam));
                response = new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
            } else {
                response = new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa Comment",
                        new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền xóa Comment ", null));
            }
        }
        return response;

    }


    private CommentsDTO buildCommentsDTO(Map<String, Object> bodyParam) {
        Integer type = StringUtil.objectToInteger(bodyParam.getOrDefault("type", ""));
        String refId = StringUtil.objectToString(bodyParam.getOrDefault("refId", ""));
        String content = StringUtil.objectToString(bodyParam.getOrDefault("content", ""));
        String contentUnsigned = StringUtil.objectToString(bodyParam.getOrDefault("contentUnsigned", ""));

        CommentsDTO commentsDTO = CommentsDTO.builder()
                .type(type)
                .refId(refId)
                .content(content)
                .contentUnsigned(contentUnsigned)
                .build();

        return commentsDTO;
    }

    private CommentsDetailDTO transform(Comments comments) {
        CommentsDetailDTO commentsDetailDTO = CommentsDetailDTO.builder()
                .id(comments.getId())
                .type(comments.getType())
                .refId(comments.getRefId())
                .content(comments.getContent())
                .contentUnsigned(comments.getContentUnsigned())
                .createdUser(comments.getCreatedUser())
                .updatedUser(comments.getUpdatedUser())
                .createdTime(comments.getCreatedTime())
                .updatedTime(comments.getUpdatedTime())
                .build();

        return commentsDetailDTO;
    }


}
