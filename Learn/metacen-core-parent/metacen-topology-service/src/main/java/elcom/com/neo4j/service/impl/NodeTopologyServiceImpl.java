package elcom.com.neo4j.service.impl;

import elcom.com.neo4j.model.NodeInfo;
import elcom.com.neo4j.model.Topology;
import elcom.com.neo4j.repositoryPostgre.NodeTopologyRepository;
import elcom.com.neo4j.repositoryPostgre.TopologyRepository;
import elcom.com.neo4j.service.NodeTopologyService;
import elcom.com.neo4j.service.TopologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NodeTopologyServiceImpl implements NodeTopologyService {
    @Autowired
    private NodeTopologyRepository nodeTopologyRepository;

    @Override
    public void saveTopology(NodeInfo topology) {
        nodeTopologyRepository.save(topology);
    }

    @Override
    public void updateTopology(NodeInfo topology) {
        nodeTopologyRepository.save(topology);
    }

    @Override
    public NodeInfo findById(String ids) {
        List<NodeInfo> nodeInfos = nodeTopologyRepository.findByNodeId(ids);
        if(nodeInfos!=null&&!nodeInfos.isEmpty()){
            return nodeInfos.get(0);
        }else
            return null;
    }

    @Override
    public Optional<NodeInfo> findByIdCheck(String ids) {
        return nodeTopologyRepository.findById(ids);
    }

    @Override
    public Page<NodeInfo> findAll(Pageable paging) {
        return null;
    }

    @Override
    public void delete(List<String> ids) {
        nodeTopologyRepository.deleteAllById(ids);
    }
}
