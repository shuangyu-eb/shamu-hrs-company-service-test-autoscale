package com.tardisone.companyservice.repository;

import com.tardisone.companyservice.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity, IdT extends Long> extends
        JpaRepository<T, IdT> {

}
