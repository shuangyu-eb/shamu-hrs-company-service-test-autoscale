package shamu.company.job.service;

import java.util.List;
import shamu.company.job.pojo.JobInformationPojo;
import shamu.company.user.entity.User;

public interface JobUserService {

  JobInformationPojo getJobInfoModal(Long userId);

  JobInformationPojo getJobInfoByUserId(Long userId);

  List getOfficeAddresses(User user);

  List getEmploymentTypes(User user);

  List getDepartments(User user);

  List getCompensationFrequences(User user);

  List getStateProvinces();

  List getManagers(User user);
}
