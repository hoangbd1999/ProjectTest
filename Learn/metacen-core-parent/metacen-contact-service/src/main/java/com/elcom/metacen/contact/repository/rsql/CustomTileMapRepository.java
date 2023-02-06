package com.elcom.metacen.contact.repository.rsql;

import com.elcom.metacen.contact.model.TileMap;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapFilterDTO;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapResponseDTO;
import com.elcom.metacen.contact.repository.BaseCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 *
 * @author hoangbd
 */
public interface CustomTileMapRepository extends BaseCustomRepository<TileMap> {

    Page<TileMapResponseDTO> search(TileMapFilterDTO tileMapFilterDTO, Pageable pageable);

}
