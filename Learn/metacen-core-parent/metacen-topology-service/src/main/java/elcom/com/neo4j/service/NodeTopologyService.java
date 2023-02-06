package elcom.com.neo4j.service;

import elcom.com.neo4j.model.NodeInfo;
import elcom.com.neo4j.model.Topology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NodeTopologyService {
    void saveTopology(NodeInfo topology);
    void updateTopology(NodeInfo topology);
    NodeInfo findById(String ids);
    Optional<NodeInfo> findByIdCheck(String ids);
    Page<NodeInfo> findAll(Pageable paging);
    void delete(List<String> ids);
}
