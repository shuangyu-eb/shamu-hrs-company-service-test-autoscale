package shamu.company.attendance.service;

import org.springframework.stereotype.Service;
import shamu.company.attendance.entity.EmployeeTimeEntry;
import shamu.company.attendance.repository.EmployeeTimeEntryRepository;

@Service
public class EmployeeTimeEntryService {

  private final EmployeeTimeEntryRepository employeeTimeEntryRepository;

  public EmployeeTimeEntryService(final EmployeeTimeEntryRepository employeeTimeEntryRepository) {
    this.employeeTimeEntryRepository = employeeTimeEntryRepository;
  }

  public EmployeeTimeEntry saveEntry(final EmployeeTimeEntry employeeTimeEntry) {
    return employeeTimeEntryRepository.save(employeeTimeEntry);
  }
}
