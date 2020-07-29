package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.repository.EmployeeTimeEntryRepository;
import shamu.company.common.exception.errormapping.ResourceNotFoundException;

@Service
public class EmployeeTimeEntryService {

  private final EmployeeTimeEntryRepository employeeTimeEntryRepository;

  public EmployeeTimeEntryService(final EmployeeTimeEntryRepository employeeTimeEntryRepository) {
    this.employeeTimeEntryRepository = employeeTimeEntryRepository;
  }

  public EmployeeTimeEntry saveEntry(final EmployeeTimeEntry employeeTimeEntry) {
    return employeeTimeEntryRepository.save(employeeTimeEntry);
  }

  public EmployeeTimeEntry findById(final String entryId) {
    return employeeTimeEntryRepository
        .findById(entryId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    String.format("Entry with id %s not found!", entryId), entryId, "entry"));
  }

  public void deleteMyHourEntry(final String entryId) {
    employeeTimeEntryRepository.delete(entryId);
  }
}
