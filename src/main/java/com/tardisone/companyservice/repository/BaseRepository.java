package com.tardisone.companyservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
}
