package com.elcom.metacen.contact.validation;

import com.elcom.metacen.contact.model.dto.EventDTO.EventFilterDTO;
import com.elcom.metacen.contact.model.dto.EventDTO.EventRequestDTO;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileMapValidation extends AbstractValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(TileMapValidation.class);

    public String validateFilterTileMap(TileMapFilterDTO tileMapFilterDTO ) {
        return !isValid() ? this.buildValidationMessage() : null;
    }
}
