package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.City;

public interface CityRepository extends BaseRepository<City, Long> {
    City findCityByName(String name);
}
