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
import shamu.company.benefit.dto.BenefitDependentCreateDto;
import shamu.company.benefit.dto.BenefitDependentDto;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.mapper.BenefitPlanDependentMapper;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.hashids.HashidsFormat;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;

@RestApiController
public class BenefitPlanDependentController {

  private final UserService userService;

  private final BenefitPlanDependentService benefitPlanDependentService;

  private final BenefitPlanDependentMapper benefitPlanDependentMapper;

  @Autowired
  public BenefitPlanDependentController(final UserService userService,
      final BenefitPlanDependentService benefitPlanDependentService,
      final BenefitPlanDependentMapper benefitPlanDependentMapper) {
    this.userService = userService;
    this.benefitPlanDependentService = benefitPlanDependentService;
    this.benefitPlanDependentMapper = benefitPlanDependentMapper;
  }

  @PostMapping("employee/{userId}/user-dependent-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity createBenefitDependents(@PathVariable @HashidsFormat final Long userId,
      @RequestBody final BenefitDependentCreateDto benefitDependentCreateDto) {
    final User user = userService.findUserById(userId);
    benefitDependentCreateDto.setEmployee(user);
    final BenefitPlanDependent dependentContact = benefitPlanDependentMapper
        .createFromBenefitDependentCreateDto(benefitDependentCreateDto);
    benefitPlanDependentService.createBenefitPlanDependent(dependentContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }


  @GetMapping("users/{userId}/user-dependent-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public List<BenefitDependentDto> getDependentContacts(
      @PathVariable @HashidsFormat final Long userId) {
    final List<BenefitPlanDependent> userDependentContacts = benefitPlanDependentService
        .getDependentListsByEmployeeId(userId);

    return userDependentContacts.stream()
        .map(benefitPlanDependentMapper::convertToBenefitDependentDto)
        .collect(Collectors.toList());
  }


  @PatchMapping("users/{userId}/user-dependent-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity updateDependentContact(@PathVariable @HashidsFormat final Long userId,
      @RequestBody final BenefitDependentCreateDto benefitDependentCreateDto) {
    final BenefitPlanDependent dependent = benefitPlanDependentService
        .findDependentById(benefitDependentCreateDto.getId());
    benefitPlanDependentMapper.updateFromBenefitDependentCreateDto(dependent,
        benefitDependentCreateDto);
    benefitPlanDependentService.updateDependentContact(dependent);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping("user-dependent-contacts/{dependentId}/dependent")
  @PreAuthorize("hasPermission(#dependentId,'BENEFIT_DEPENDENT', 'EDIT_USER')"
      + " or hasPermission(#dependentId,'BENEFIT_DEPENDENT', 'EDIT_SELF')")
  public HttpEntity deleteEmergencyContacts(@PathVariable @HashidsFormat final Long dependentId) {
    benefitPlanDependentService.deleteDependentContact(dependentId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
