package elcom.com.neo4j.repositorymogo;

import elcom.com.neo4j.dto.MappingVsatFilterDTO;
import elcom.com.neo4j.dto.MappingVsatResponseDTO;
import elcom.com.neo4j.model.MappingVsatMetacen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Admin
 */
public interface CustomMappingVsatMetacenRepository extends BaseCustomRepository<MappingVsatMetacen> {

    Page<MappingVsatResponseDTO> search(MappingVsatFilterDTO mappingVsatFilterDTO, Pageable pageable);

}
