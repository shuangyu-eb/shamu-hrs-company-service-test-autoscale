package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.City;

public interface CityService {
    City findCityByName(String name);
}