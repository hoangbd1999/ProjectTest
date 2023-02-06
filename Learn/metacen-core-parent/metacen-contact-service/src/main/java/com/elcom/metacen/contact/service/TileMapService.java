/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.contact.service;

import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapFilterDTO;
import com.elcom.metacen.contact.model.dto.TileMapDTO.TileMapResponseDTO;
import org.springframework.data.domain.Page;

/**
 * @author hoangbd
 */
public interface TileMapService {

    Page<TileMapResponseDTO> findListTileMap(TileMapFilterDTO tileMapFilterDTO);
}
