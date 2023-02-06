package com.elcom.metacen.content.service;

import com.elcom.metacen.content.dto.ContentDTO;
import org.apache.flink.api.java.tuple.Tuple3;

import java.util.List;

public interface ProcessContentService {

    public List<Tuple3<String,Boolean,Object>> processContent(List<ContentDTO> contentDTOS) throws Exception;
}
