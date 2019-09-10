package shamu.company.common.repository;

import java.util.List;
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

  @Override
  @Transactional
  default void delete(final T entity) {
    delete(entity.getId());
  }

  @Transactional
  default void delete(final Iterable<? extends T> entities) {
    entities.forEach(entitiy -> delete(entitiy.getId()));
  }

  @Query(value = "update #{#entityName} e set e.deletedAt=current_timestamp where e.id in ?1 "
      + "and e.deletedAt is null")
  @Transactional
  @Modifying
  void deleteInBatch(List<Long> ids);

  @Override
  @Query(value = "update #{#entityName} set deletedAt=current_timestamp where deletedAt is null ")
  @Transactional
  @Modifying
  void deleteAll();
}
