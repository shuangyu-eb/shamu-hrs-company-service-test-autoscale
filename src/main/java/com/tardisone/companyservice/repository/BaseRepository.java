package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
interface BaseRepository<T extends BaseEntity, ID extends Long> extends JpaRepository<T, ID> {
    @Query(value = "update #{#entityName} set deletedAt=current_timestamp where id = ?1 "
            + "and deletedAt is null")
    @Transactional
    @Modifying
    void delete(long id);

    @Transactional
    default void delete(T entity) {
        delete(entity.getId());
    }

    @Transactional
    default void delete(Iterable<? extends T> entities) {
        entities.forEach(entitiy -> delete(entitiy.getId()));
    }

    @Override
    @Query(value = "update #{#entityName} set deletedAt=current_timestamp where deletedAt is null ")
    @Transactional
    @Modifying
    void deleteAll();
}
