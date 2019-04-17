package com.tardisone.companyservice.service;

import com.tardisone.companyservice.entity.User;

import java.util.List;

public interface EmployeeeInformationService {

    User findEmployeeInfoByEmployeeNumber(String uid);

    List<User> findEmployeesByManagerId(Long mid);
}
