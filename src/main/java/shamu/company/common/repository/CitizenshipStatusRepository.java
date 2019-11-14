package shamu.company.common.repository;

import java.util.List;
import shamu.company.user.entity.CitizenshipStatus;

public interface CitizenshipStatusRepository extends BaseRepository<CitizenshipStatus, String> {

  List<CitizenshipStatus> findAll();
}
