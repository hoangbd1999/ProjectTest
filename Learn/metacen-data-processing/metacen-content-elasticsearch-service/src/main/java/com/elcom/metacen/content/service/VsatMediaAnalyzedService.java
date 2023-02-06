package com.elcom.metacen.content.service;

import com.elcom.metacen.content.model.VsatMediaAnalyzed;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;

public interface VsatMediaAnalyzedService {
    Iterable<VsatMediaAnalyzed> saveAll(List<VsatMediaAnalyzed> recognitionList);
    SearchHits<VsatMediaAnalyzed> test();
    List<VsatMediaAnalyzed> test1();
}
