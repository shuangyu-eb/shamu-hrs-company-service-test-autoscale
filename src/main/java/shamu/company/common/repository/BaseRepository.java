package shamu.company.common.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import shamu.company.common.entity.BaseEntity;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, I extends String>
    extends JpaRepository<T, I> {

  @Query(value = "delete from #{#entityName} where id = ?1 ")
  @Transactional
  @Modifying
  void delete(String id);

  @Override
  @Transactional
  default void delete(final T entity) {
    delete(entity.getId());
  }

  @Transactional
  default void delete(final Iterable<? extends T> entities) {
    entities.forEach(entitiy -> delete(entitiy.getId()));
  }

  @Query(value = "delete from #{#entityName} e where e.id in ?1 ")
  @Transactional
  @Modifying
  void deleteInBatch(List<String> ids);
}
