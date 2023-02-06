package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.model.Side;
import com.elcom.metacen.contact.model.dto.*;
import com.elcom.metacen.contact.service.SideService;
import com.elcom.metacen.contact.validation.SideValidation;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class SideController extends BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SideController.class);

    @Autowired
    private SideService sideService;

    public ResponseMessage insertSide(Map<String, String> headerParam, Map<String, Object> bodyParam, String requestPath) {
        LOGGER.info("Create side with request >>> {}", bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "POST", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            SideDTO sideDTO = buildSideDTO(bodyParam);
            String validationMsg = new SideValidation().validateSide(sideDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            Side side = sideService.save(sideDTO);
            if (side != null) {
                return new ResponseMessage(new MessageContent(entityToDto(side)));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage updateSide(Map<String, String> headerParam, Map<String, Object> bodyParam, String pathParam, String requestPath) {
        LOGGER.info("Update Side id {} with request >>> {}", pathParam, bodyParam);

        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "PUT", dto.getUuid(), requestPath);
        if (abacStatus != null && abacStatus.getStatus()) {
            String uuidKey = pathParam;
            Side side = sideService.findById(uuidKey);
            if (side == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Side không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Side không tồn tại", null));
            }
            SideDTO sideDTO = buildSideDTO(bodyParam);
            String validationMsg = new SideValidation().validateSide(sideDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }

            Side result = sideService.updateSide(side, sideDTO);
            if (result != null) {
                return new ResponseMessage(new MessageContent(entityToDto(result)));
            } else {
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getSideById(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DETAIL", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            Side side = sideService.findById(pathParam);
            if (side == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Side không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Side không tồn tại", null));
            }
            return new ResponseMessage(new MessageContent(side));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage getSideList(String requestPath, Map<String, String> headerParam, String urlParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }

        // Check ABAC
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "LIST", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            SideFilterDTO sideFilterDTO = buildSideFilterRequest(urlParam);
            String validationMsg = new SideValidation().validateFilterSide(sideFilterDTO);
            if (validationMsg != null) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
            }
            Page<Side> pagedResult = sideService.findListSide(sideFilterDTO);
            List<SideRequestDTO> results = pagedResult.getContent()
                    .parallelStream()
                    .map(this::entityToDto)
                    .collect(Collectors.toList());
            return new ResponseMessage(new MessageContent(results, pagedResult.getTotalElements()));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    public ResponseMessage deleteSide(String requestPath, Map<String, String> headerParam, String pathParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
        Map<String, Object> body = new HashMap<String, Object>();
        ABACResponseDTO abacStatus = authorizeABAC(body, "DELETE", dto.getUuid(), requestPath);

        if (abacStatus != null && abacStatus.getStatus()) {
            Side side = sideService.findById(pathParam);
            if (side == null) {
                return new ResponseMessage(HttpStatus.NOT_FOUND.value(), "Side không tồn tại",
                        new MessageContent(HttpStatus.NOT_FOUND.value(), "Side không tồn tại", null));
            }
            this.sideService.delete(side);
            return new ResponseMessage(new MessageContent(HttpStatus.OK.value(), "Xóa dữ liệu thành công", null));
        } else {
            return new ResponseMessage(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này",
                    new MessageContent(HttpStatus.FORBIDDEN.value(), "Bạn không có quyền thực hiện hành động này", null));
        }
    }

    private SideDTO buildSideDTO(Map<String, Object> bodyParam) {
        String name = (String) bodyParam.getOrDefault("name", "");
        String note = (String) bodyParam.getOrDefault("note", "");

        SideDTO sideDTO = SideDTO.builder()
                .name(name)
                .note(note)
                .build();

        return sideDTO;
    }

    private SideFilterDTO buildSideFilterRequest(String urlParam) {
        Map<String, String> params = StringUtil.getUrlParamValues(urlParam);

        Integer page = params.get("page") != null ? Integer.parseInt(params.get("page")) : 0;
        Integer size = params.get("size") != null ? Integer.parseInt(params.get("size")) : 20;
        String term = params.getOrDefault("term", "");

        SideFilterDTO sideFilterDTO = SideFilterDTO.builder()
                .page(page)
                .size(size)
                .term(term)
                .build();
        return sideFilterDTO;
    }

    private SideRequestDTO entityToDto(Side side) {
        SideRequestDTO sideRequestDTO = SideRequestDTO.builder()
                .id(side.getId())
                .uuidKey(side.getUuidKey())
                .name(side.getName())
                .note(side.getNote())
                .createdBy(side.getModifiedBy())
                .createdDate(side.getCreatedDate())
                .modifiedDate(side.getModifiedDate())
                .modifiedBy(side.getModifiedBy())
                .build();

        return sideRequestDTO;
    }
}
