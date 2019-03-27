package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.MartialStatus;

public interface MartialStatusRepository extends BaseRepository<MartialStatus, Long> {
    MartialStatus findMartialStatusById(Long id);
}
