package com.tardisone.companyservice.service.impl;

import com.tardisone.companyservice.entity.StateProvince;
import com.tardisone.companyservice.repository.StateProvinceRepository;
import com.tardisone.companyservice.service.StateProvinceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StateProvinceServiceImpl implements StateProvinceService {

    @Autowired
    StateProvinceRepository stateProvinceRepository;

    @Override
    public StateProvince getStateProvince(Long id) {
        Optional<StateProvince> optionalStateProvince = stateProvinceRepository.findById(id);
        return optionalStateProvince.orElse(new StateProvince());
    }
}
