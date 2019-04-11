package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.StateProvince;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "states_provinces")
public interface StateProvinceRepository extends BaseRepository<StateProvince, Long> {
}
