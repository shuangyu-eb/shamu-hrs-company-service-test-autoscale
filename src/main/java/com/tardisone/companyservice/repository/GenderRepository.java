package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Gender;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "genders")
public interface GenderRepository extends BaseRepository<Gender, Long>{
}
