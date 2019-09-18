package shamu.company.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import shamu.company.admin.dto.SuperAdminUserDto;

public interface SuperAdminService {

  Page<SuperAdminUserDto> getUsersBy(String keyword, Pageable pageable);

}
