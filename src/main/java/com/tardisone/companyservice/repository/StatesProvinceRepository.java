package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.StatesProvince;

public interface StatesProvinceRepository extends BaseRepository<StatesProvince, Long> {
    StatesProvince findStatesProvinceById(Long id);
}
