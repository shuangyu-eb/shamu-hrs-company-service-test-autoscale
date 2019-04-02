package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.City;

public interface CityRepository extends BaseRepository<City, Long> {
    City findCityById(Long id);
    City findCityByName(String name);
}
