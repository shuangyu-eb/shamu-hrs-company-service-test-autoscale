package shamu.company.user.service;

import shamu.company.job.JobUserDTO;
import shamu.company.user.dto.PersonalInformationDTO;
import shamu.company.user.entity.User;

import java.util.List;

public interface UserService {
    User findUserByEmail(String email);

    void sendVerifyEmail(String email);

    void finishUserVerification(String activationToken);

    List<JobUserDTO> findAllEmployees();

    Boolean existsByEmailWork(String email);

    PersonalInformationDTO getPersonalInformation(Long userId);
}
