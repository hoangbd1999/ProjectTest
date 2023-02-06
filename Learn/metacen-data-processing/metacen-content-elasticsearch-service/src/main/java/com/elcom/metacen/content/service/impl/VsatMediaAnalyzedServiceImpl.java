package com.elcom.metacen.content.service.impl;

import com.elcom.metacen.content.model.VsatMediaAnalyzed;
import com.elcom.metacen.content.repository.VsatMediaAnalyzedRepository;
import com.elcom.metacen.content.repository.VsatMediaAnalyzedRepositoryCustomer;
import com.elcom.metacen.content.service.VsatMediaAnalyzedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VsatMediaAnalyzedServiceImpl implements VsatMediaAnalyzedService {
    @Autowired
    private VsatMediaAnalyzedRepository vsatMediaAnalyzedRepository;
    @Autowired
    private VsatMediaAnalyzedRepositoryCustomer vsatMediaAnalyzedRepositoryCustomer;
    @Override
    public Iterable<VsatMediaAnalyzed> saveAll(List<VsatMediaAnalyzed> recognitionList) {
        return vsatMediaAnalyzedRepository.saveAll(recognitionList);
    }

    @Override
    public SearchHits<VsatMediaAnalyzed> test() {
        return vsatMediaAnalyzedRepositoryCustomer.findProductsByBrand();
    }

    @Override
    public List<VsatMediaAnalyzed> test1() {
        return vsatMediaAnalyzedRepository.findByFileContentGB18030ContainsOrFileContentUtf8Contains("!@#$%^&*()test:tes:add:ass","test");
    }
}
