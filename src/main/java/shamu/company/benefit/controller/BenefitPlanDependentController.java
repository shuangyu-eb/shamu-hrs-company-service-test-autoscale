package shamu.company.benefit.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import shamu.company.benefit.dto.BenefitDependentDto;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.pojo.BenefitDependentPojo;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class BenefitPlanDependentController {

  private final UserService userService;

  private final BenefitPlanDependentService benefitPlanDependentService;

  @Autowired
  public BenefitPlanDependentController(UserService userService,
      BenefitPlanDependentService benefitPlanDependentService) {
    this.userService = userService;
    this.benefitPlanDependentService = benefitPlanDependentService;
  }

  @PostMapping("employee/{userId}/user-dependent-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity createBenefitDependents(@PathVariable @HashidsFormat Long userId,
      @RequestBody BenefitDependentPojo dependentContactPojo) {
    User user = userService.findUserById(userId);
    dependentContactPojo.setEmployee(user);
    BenefitPlanDependent dependentContact = dependentContactPojo.getBenefitDependent();
    benefitPlanDependentService.createBenefitPlanDependent(dependentContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }


  @GetMapping("users/{userId}/user-dependent-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public List<BenefitDependentDto> getDependentContacts(
      @PathVariable @HashidsFormat Long userId) {
    List<BenefitPlanDependent> userDependentContacts = benefitPlanDependentService
        .getDependentListsByEmployeeId(userId);

    return userDependentContacts.stream()
        .map(BenefitDependentDto::new)
        .collect(Collectors.toList());
  }


  @PatchMapping("users/{userId}/user-dependent-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity updateDependentContact(@PathVariable @HashidsFormat Long userId,
      @RequestBody BenefitDependentPojo dependentContactPojo) {
    BenefitPlanDependent dependent = benefitPlanDependentService
        .findDependentById(dependentContactPojo.getId());
    BenefitPlanDependent newDependent = dependentContactPojo.getUpdatedDependent(dependent);
    benefitPlanDependentService.updateDependentContact(newDependent);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("user-dependent-contacts/{dependentId}/dependent")
  @PreAuthorize("hasPermission(#dependentId,'BENEFIT_DEPENDENT', 'EDIT_USER')"
      + " or hasPermission(#dependentId,'BENEFIT_DEPENDENT', 'EDIT_SELF')")
  public HttpEntity deleteEmergencyContacts(@PathVariable @HashidsFormat Long dependentId) {
    benefitPlanDependentService.deleteDependentContact(dependentId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
