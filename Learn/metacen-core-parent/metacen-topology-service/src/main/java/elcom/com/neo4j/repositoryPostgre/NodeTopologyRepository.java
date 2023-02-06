package elcom.com.neo4j.repositoryPostgre;

import elcom.com.neo4j.model.NodeInfo;
import elcom.com.neo4j.model.Topology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodeTopologyRepository extends PagingAndSortingRepository<NodeInfo, String> {

    Page<NodeInfo> findAll(Pageable paging);
    List<NodeInfo> findByNodeId(String nodeId);
}
