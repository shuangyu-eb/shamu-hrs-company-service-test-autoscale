package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.Gender;

import java.util.List;

public interface GenderRepository extends BaseRepository<Gender, Long> {
    Gender findGenderById(Long id);
}
