package elcom.com.neo4j.service;

import elcom.com.neo4j.model.Topology;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface TopologyService {
    void saveTopology(Topology topology);
    void updateTopology(Topology topology);
    Optional<Topology> findById(String ids);
    Page<Topology> findAll(Pageable paging);
    void delete(List<String> ids);
}
