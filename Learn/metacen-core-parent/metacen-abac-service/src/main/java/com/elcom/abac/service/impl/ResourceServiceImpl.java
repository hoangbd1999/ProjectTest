package com.elcom.abac.service.impl;

import com.elcom.abac.dto.Condition;
import com.elcom.abac.dto.ParamValueDto;
import com.elcom.abac.model.Resource;
import com.elcom.abac.repository.ResourceRepository;
import com.elcom.abac.service.PolicyService;
import com.elcom.abac.service.ResourceServiceAbac;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceServiceImpl implements ResourceServiceAbac {

    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    @Cacheable(value = "resources")
    public List<Resource> findAll() {

        return resourceRepository.findAllByOrderByUrlPatternsLengthDesc();
    }

    @Override
    @Cacheable(value = "resources")
    public List<Resource> findAllForUser() {

        List<Resource> resources = resourceRepository.findAll();
        for (Resource resource: resources
        ) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                if(resource.getParamValueType()!=null) {
                    ParamValueDto paramValueDto = mapper.readValue(resource.getParamValueType(), ParamValueDto.class);
                    resource.setParamValueDto(paramValueDto);
                }

            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        }
        return resources;
    }
    @Override
    public Optional<Resource> findById(Integer id) {
        return resourceRepository.findById(id);
    }

    @Override
    public List<Resource> findByCodeIn(List<String> resourceCode) {
        return resourceRepository.findByCodeIn(resourceCode);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Resource saveResource(Resource resource) {
        resource.setCreatedAt(new Date());
        try {
            ObjectMapper mapper = new ObjectMapper();
            Condition condition;
                if(!resource.getCreatePolicyType().equals("*"))
                    condition = mapper.readValue(resource.getCreatePolicyType(), Condition.class);
                if(!resource.getUpdatePolicyType().equals("*"))
                    condition = mapper.readValue(resource.getUpdatePolicyType(),Condition.class);
                if(!resource.getDetailPolicyType().equals("*"))
                    condition = mapper.readValue(resource.getDetailPolicyType(),Condition.class);
                if(!resource.getListPolicyType().equals("*"))
                    condition = mapper.readValue(resource.getListPolicyType(),Condition.class);
                if(!resource.getDeletePolicyType().equals("*"))
                    condition = mapper.readValue(resource.getDeletePolicyType(),Condition.class);

            } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
        return resourceRepository.save(resource);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Resource updateResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    @Override
    @CacheEvict(value = "resources")
    @Transactional(rollbackFor = {Exception.class})
    public boolean deleteResource(Integer id) {
         resourceRepository.deleteById(id);
         return true;
    }
    @Override
    @CacheEvict(value = "resources")
    @Transactional(rollbackFor = {Exception.class})
    public boolean deleteResource() {
        return true;
    }
}
