package shamu.company.info.service;

import java.util.List;
import shamu.company.info.entity.UserEmergencyContact;

public interface UserEmergencyContactService {

  List<UserEmergencyContact> getUserEmergencyContacts(Long userId);

  void createUserEmergencyContact(Long userId, UserEmergencyContact userEmergencyContact);

  void deleteEmergencyContact(Long userId, Long id);

  void updateEmergencyContact(Long userId, UserEmergencyContact emergencyContact);
}
