package shamu.company.user.service;

import java.util.List;
import shamu.company.job.JobUserDto;
import shamu.company.user.dto.PersonalInformationDto;
import shamu.company.user.entity.User;

public interface UserService {

  User save(User user);

  User findUserById(Long id);

  User findUserByEmail(String email);

  void sendVerifyEmail(String email);

  void finishUserVerification(String activationToken);

  List<JobUserDto> findAllEmployees();

  Boolean existsByEmailWork(String email);

  PersonalInformationDto getPersonalInformation(Long userId);

  User findEmployeeInfoByUserId(Long uid);

  JobUserDto findEmployeeInfoByEmployeeId(Long id);

  List<JobUserDto> findDirectReportsByManagerId(Long mid);
}
