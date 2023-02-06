/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elcom.com.neo4j.service;
import elcom.com.neo4j.dto.MappingVsatFilterDTO;
import elcom.com.neo4j.dto.MappingVsatRequestDTO;
import elcom.com.neo4j.dto.MappingVsatResponseDTO;
import elcom.com.neo4j.model.MappingVsatMetacen;
import org.springframework.data.domain.Page;

/**
 * @author Admin
 */
public interface MappingVsatMetacenService {

    MappingVsatMetacen save(MappingVsatRequestDTO mappingVsatRequestDTO, String createBy);

    MappingVsatMetacen updateMappingVsat(MappingVsatMetacen mappingVsatMetacen, MappingVsatRequestDTO mappingVsatRequestDTO, String modifiedBy);

    Page<MappingVsatResponseDTO> findListMappingVsat(MappingVsatFilterDTO mappingVsatFilterDTO);

    MappingVsatMetacen delete(MappingVsatMetacen mappingVsatMetacen);
}
