package com.elcom.metacen.contact.model.dto.TileMapDTO;

import com.elcom.metacen.contact.model.dto.BaseDTO;
import com.elcom.metacen.contact.model.dto.FileDTO;
import com.elcom.metacen.contact.model.dto.KeywordDTO;
import com.elcom.metacen.contact.model.dto.ObjectRelationshipDeltailDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;

/**
 *
 * @author hoangbd
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TileMapResponseDTO {
    protected String id;
    private String name;
    private String coordinates;
}


