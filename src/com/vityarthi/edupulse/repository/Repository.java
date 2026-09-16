package com.vityarthi.edupulse.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Generic Repository interface defining CRUD and query contracts.
 * Demonstrates Generics and Type Safety.
 */
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    List<T> findBy(Predicate<T> predicate);
    boolean deleteById(ID id);
    boolean existsById(ID id);
    long count();
    void clear();
}
