package shamu.company.user.service;

import java.util.List;
import shamu.company.company.entity.Company;
import shamu.company.job.JobUserDto;
import shamu.company.user.dto.PersonalInformationDto;
import shamu.company.user.entity.User;

public interface UserService {

  User findUserById(Long id);

  User findUserByEmail(String email);

  User findUserByUserPersonalInformationId(Long userPersonalInformationId);

  void sendVerifyEmail(String email);

  void finishUserVerification(String activationToken);

  List<JobUserDto> findEmployeesByCompany(Company company);

  Boolean existsByEmailWork(String email);

  PersonalInformationDto getPersonalInformation(Long userId);

  User findEmployeeInfoByUserId(Long id);

  JobUserDto findEmployeeInfoByEmployeeId(Long id);

  List<JobUserDto> findDirectReportsByManagerId(Long id);
}
