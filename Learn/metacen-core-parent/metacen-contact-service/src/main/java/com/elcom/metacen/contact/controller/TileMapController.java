package com.elcom.metacen.contact.controller;

import com.elcom.metacen.contact.constant.Constant;

import com.elcom.metacen.contact.model.dto.AuthorizationResponseDTO;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapFilterDTO;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapResponseDTO;

import com.elcom.metacen.contact.service.TileMapService;
import com.elcom.metacen.contact.validation.TileMapValidation;
import com.elcom.metacen.message.MessageContent;
import com.elcom.metacen.message.ResponseMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class TileMapController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TileMapController.class);

    @Autowired
    private TileMapService tileMapService;

    public ResponseMessage filterTileMap(Map<String, String> headerParam, Map<String, Object> bodyParam) {
        // Check isLogged
        AuthorizationResponseDTO dto = authenToken(headerParam);
        if (dto == null) {
            return unauthorizedResponse();
        }
            if (bodyParam == null || bodyParam.isEmpty()) {
                return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE,
                        new MessageContent(HttpStatus.BAD_REQUEST.value(), Constant.VALIDATION_INVALID_PARAM_VALUE, null));
            }
            try {
                TileMapFilterDTO tileMapFilterDTO = buildTileMapFilterDTO(bodyParam);
                String validationMsg = new TileMapValidation().validateFilterTileMap(tileMapFilterDTO);
                if (validationMsg != null) {
                    return new ResponseMessage(HttpStatus.BAD_REQUEST.value(), validationMsg,
                            new MessageContent(HttpStatus.BAD_REQUEST.value(), validationMsg, null));
                }
                Page<TileMapResponseDTO> pagedResult = tileMapService.findListTileMap(tileMapFilterDTO);
                return new ResponseMessage(new MessageContent(pagedResult.getContent(), pagedResult.getTotalElements()));

            } catch (Exception e) {
                LOGGER.error("Filter failed >>> {}", e.toString());
                return new ResponseMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                        new MessageContent(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), null));
            }
    }

    private TileMapFilterDTO buildTileMapFilterDTO(Map<String, Object> bodyParam) {
        Integer page = bodyParam.get("page") != null ? (Integer) bodyParam.get("page") : 0;
        Integer size = bodyParam.get("size") != null ? (Integer) bodyParam.get("size") : 20;
        String name = (String) bodyParam.getOrDefault("name", "");
        String term = (String) bodyParam.getOrDefault("term", "");


        TileMapFilterDTO tileMapFilterDTO = TileMapFilterDTO.builder()
                .page(page)
                .size(size)
                .name(name)
                .term(term)
                .build();
        return tileMapFilterDTO;
    }

}
