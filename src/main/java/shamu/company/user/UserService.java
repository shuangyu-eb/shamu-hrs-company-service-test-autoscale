package shamu.company.user;

import shamu.company.job.JobUserDTO;
import shamu.company.user.entity.User;

import java.util.List;

public interface UserService {
    User findUserByEmail(String email);

    void sendVerifyEmail(String email);

    void finishUserVerification(String activationToken);

    List<JobUserDTO> findAllEmployees();

    Boolean existsByEmailWork(String email);

}
