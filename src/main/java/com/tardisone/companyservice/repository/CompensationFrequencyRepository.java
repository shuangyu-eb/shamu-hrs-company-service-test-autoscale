package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.CompensationFrequency;
import org.springframework.stereotype.Repository;

import javax.persistence.Table;

@Repository
@Table(name = "compensation_frequency")
public interface CompensationFrequencyRepository extends BaseRepository<CompensationFrequency, Long> {
}
