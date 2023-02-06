package elcom.com.neo4j.service.impl;

import elcom.com.neo4j.model.Topology;
import elcom.com.neo4j.repositoryPostgre.TopologyRepository;
import elcom.com.neo4j.service.TopologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TopologyServiceImpl implements TopologyService {
    @Autowired
    private TopologyRepository topologyRepository;

    @Override
    public void saveTopology(Topology topology) {
        topologyRepository.save(topology);
    }

    @Override
    public void updateTopology(Topology topology) {
        topologyRepository.save(topology);
    }

    @Override
    public Optional<Topology> findById(String ids) {
        return topologyRepository.findById(ids);
    }

    @Override
    public Page<Topology> findAll(Pageable paging) {
        return topologyRepository.findAll(paging);
    }

    @Override
    public void delete(List<String> ids) {
        topologyRepository.deleteAllById(ids);

    }
}
