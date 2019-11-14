package shamu.company.common.repository;

import java.util.List;
import shamu.company.info.entity.State;

public interface StateRepository extends BaseRepository<State, String> {

  List<State> findAll();
}
