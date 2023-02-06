package com.elcom.metacen.contact.service.impl;

import com.elcom.metacen.contact.service.DataOperations;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import com.google.common.collect.Lists;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Admin
 */
public abstract class DataAbstractService<T extends Serializable> implements DataOperations<T> {

    // read - one
    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(final String id) {
        return getRepository().findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findByName(final String name) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findByIdIn(List<String> ids) {
        return null;
    }

    // read - all
    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return Lists.newArrayList(getRepository().findAll());
    }

    @Override
    public Page<T> findPaginated(final int page, final int size) {
        return getRepository().findAll(PageRequest.of(page, size));
    }

    @Override
    public Page<T> findPaginated(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    // write
    @Override
    public T create(final T entity) {
        return getRepository().save(entity);
    }

    @Override
    @Transactional
    public T update(final T entity) {
        return getRepository().save(entity);
    }

    @Override
    public void delete(final T entity) {
        getRepository().delete(entity);
    }

    @Override
    public void deleteById(final String entityId) {
        getRepository().deleteById(entityId);
    }

    @Override
    public Long count() {
        return getRepository().count();
    }

    protected abstract PagingAndSortingRepository<T, String> getRepository();
}
