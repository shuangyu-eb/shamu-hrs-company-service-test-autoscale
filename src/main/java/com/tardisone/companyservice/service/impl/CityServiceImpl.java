package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.City;
import com.tardisone.companyservice.repository.CityRepository;
import com.tardisone.companyservice.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CityServiceImpl  implements CityService {

    @Autowired
    CityRepository repository;

    @Override
    public City findCityByName(String name) {
        return repository.findCityByName(name);
    }
}
