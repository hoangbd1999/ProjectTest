package com.elcom.abac.service;

import com.elcom.abac.model.Resource;

import java.util.List;
import java.util.Optional;

public interface ResourceServiceAbac {
    public List<Resource> findAll();

    public Optional<Resource> findById(Integer id);

    public List<Resource> findByCodeIn(List<String> resourceCode);

    public Resource  saveResource(Resource resource);

    public Resource updateResource(Resource resource);

    public boolean deleteResource(Integer id);

    public List<Resource> findAllForUser();

    public boolean deleteResource();
}
