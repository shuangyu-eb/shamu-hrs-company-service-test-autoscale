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
import shamu.company.benefit.dto.BenefitPlanUserDto;
import shamu.company.benefit.entity.BenefitPlanDependent;
import shamu.company.benefit.entity.DependentRelationship;
import shamu.company.benefit.entity.mapper.BenefitPlanDependentMapper;
import shamu.company.benefit.repository.BenefitPlanDependentRelationshipRepository;
import shamu.company.benefit.service.BenefitPlanDependentService;
import shamu.company.common.CommonDictionaryDto;
import shamu.company.common.config.annotations.RestApiController;
import shamu.company.crypto.EncryptorUtil;
import shamu.company.user.entity.User;
import shamu.company.user.service.UserService;
import shamu.company.utils.ReflectionUtil;

@RestApiController
public class BenefitPlanDependentController {

  private final UserService userService;

  private final BenefitPlanDependentService benefitPlanDependentService;

  private final BenefitPlanDependentMapper benefitPlanDependentMapper;

  private final EncryptorUtil encryptorUtil;

  private final BenefitPlanDependentRelationshipRepository relationshipRepository;

  @Autowired
  public BenefitPlanDependentController(final UserService userService,
      final BenefitPlanDependentService benefitPlanDependentService,
      final BenefitPlanDependentMapper benefitPlanDependentMapper,
      final EncryptorUtil encryptorUtil,
      final BenefitPlanDependentRelationshipRepository relationshipRepository) {
    this.userService = userService;
    this.benefitPlanDependentService = benefitPlanDependentService;
    this.benefitPlanDependentMapper = benefitPlanDependentMapper;
    this.encryptorUtil = encryptorUtil;
    this.relationshipRepository = relationshipRepository;
  }

  @PostMapping("employee/{userId}/user-dependent-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity createBenefitDependents(@PathVariable final String userId,
      @RequestBody final BenefitDependentCreateDto benefitDependentCreateDto) {
    final User user = userService.findById(userId);
    benefitDependentCreateDto.setEmployee(user);
    final BenefitPlanDependent dependentContact = benefitPlanDependentMapper
        .createFromBenefitDependentCreateDto(benefitDependentCreateDto);
    encryptSsn(userId, benefitDependentCreateDto, dependentContact);
    benefitPlanDependentService.createBenefitPlanDependent(dependentContact);
    return new ResponseEntity<>(HttpStatus.OK);
  }


  @GetMapping("users/{userId}/user-dependent-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public List<BenefitDependentDto> getDependentContacts(
      @PathVariable final String userId) {
    final List<BenefitPlanDependent> userDependentContacts = benefitPlanDependentService
        .getDependentListsByEmployeeId(userId);

    return userDependentContacts.stream()
        .map(benefitPlanDependentMapper::convertToBenefitDependentDto)
        .collect(Collectors.toList());
  }


  @PatchMapping("users/{userId}/user-dependent-contacts")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_USER')"
      + " or hasPermission(#userId,'USER', 'EDIT_SELF')")
  public HttpEntity updateDependentContact(@PathVariable final String userId,
      @RequestBody final BenefitDependentCreateDto benefitDependentCreateDto) {
    final BenefitPlanDependent dependent = benefitPlanDependentService
        .findDependentById(benefitDependentCreateDto.getId());
    benefitPlanDependentMapper.updateFromBenefitDependentCreateDto(dependent,
        benefitDependentCreateDto);
    encryptSsn(userId, benefitDependentCreateDto, dependent);
    benefitPlanDependentService.updateDependentContact(dependent);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private void encryptSsn(final String userId,
      final BenefitDependentCreateDto benefitDependentCreateDto,
      final BenefitPlanDependent dependent) {
    encryptorUtil.encryptSsn(userId, benefitDependentCreateDto.getSsn(), dependent);
  }

  @GetMapping("user-dependent-contacts/{dependentId}/dependent")
  @PreAuthorize("hasPermission(#dependentId,'BENEFIT_DEPENDENT', 'EDIT_USER')"
      + " or hasPermission(#dependentId,'BENEFIT_DEPENDENT', 'EDIT_SELF')")
  public BenefitDependentDto getDependentContact(@PathVariable final String dependentId) {
    return benefitPlanDependentMapper.convertToBenefitDependentDto(
      benefitPlanDependentService.findDependentById(dependentId));
  }

  @DeleteMapping("user-dependent-contacts/{dependentId}/dependent")
  @PreAuthorize("hasPermission(#dependentId,'BENEFIT_DEPENDENT', 'EDIT_USER')"
      + " or hasPermission(#dependentId,'BENEFIT_DEPENDENT', 'EDIT_SELF')")
  public HttpEntity deleteEmergencyContacts(@PathVariable final String dependentId) {
    benefitPlanDependentService.deleteDependentContact(dependentId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("dependent-relationships")
  public List<CommonDictionaryDto> getAllDependentRelationships() {
    final List<DependentRelationship> results = relationshipRepository.findAll();
    return ReflectionUtil.convertTo(results, CommonDictionaryDto.class);
  }

  @GetMapping("users/{userId}/benefit-plan-dependents")
  @PreAuthorize("hasPermission(#userId,'USER', 'EDIT_SELF')")
  public List<BenefitPlanUserDto> getUserDependentContacts(
      @PathVariable final String userId) {
    final List<BenefitPlanDependent> userDependentContacts = benefitPlanDependentService
        .getDependentListsByEmployeeId(userId);

    return userDependentContacts.stream()
        .map(benefitPlanDependentMapper::convertToBenefitPlanUserDto)
        .collect(Collectors.toList());
  }
}
