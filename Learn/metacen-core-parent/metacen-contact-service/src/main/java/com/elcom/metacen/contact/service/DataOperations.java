package com.elcom.metacen.contact.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author Admin
 */
public interface DataOperations<T extends Serializable> {

    // read - one
    public Optional<T> findById(final String id);

    public Optional<T> findByName(final String name);

    List<T> findByIdIn(List<String> ids);

    // read - all
    public List<T> findAll();

    public Page<T> findPaginated(int page, int size);

    public Page<T> findPaginated(Pageable pageable);

    // write
    public T create(final T entity);

    public T update(final T entity);

    public void delete(final T entity);

    public void deleteById(final String entityId);

    public Long count();

}
