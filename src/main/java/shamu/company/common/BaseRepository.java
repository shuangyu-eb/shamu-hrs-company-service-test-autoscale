package shamu.company.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.entity.BaseEntity;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, IdT extends Long> extends
    JpaRepository<T, IdT> {

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
