package com.elcom.metacen.contact.model.dto.TileMapDTO;

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
public class TileMapFilterDTO {
    private Integer page;
    private Integer size;
    private String name;
    private String term;
}
