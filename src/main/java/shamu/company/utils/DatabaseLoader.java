package shamu.company.utils;

import shamu.company.company.entity.CompanySize;
import shamu.company.company.CompanySizeRepository;
import shamu.company.user.entity.UserRole;
import shamu.company.user.entity.UserStatus;
import shamu.company.user.repository.UserRoleRepository;
import shamu.company.user.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DatabaseLoader implements CommandLineRunner {
    @Autowired
    UserStatusRepository userStatusRepository;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    CompanySizeRepository companySizeRepository;

    @Override
    public void run(String... args) {
        this.loadUserStatuses();
        this.loadUserRoles();
        this.loadCompanySizes();
    }

    private void loadUserStatuses() {
        ArrayList<String> userStatusNames = new ArrayList<>(Arrays.asList("ACTIVE", "DISABLED", "PENDING_VERIFICATION"));
        List<UserStatus> userStatuses = userStatusRepository.findAllByName(userStatusNames);
        userStatuses.forEach((userStatus) -> {
            userStatusNames.remove(userStatus.getName());
        });

        List<UserStatus> userStatusesToAdd = userStatusNames.stream()
                .map((statusName) -> new UserStatus(statusName)).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(userStatusesToAdd)) {
            userStatusRepository.saveAll(userStatusesToAdd);
        }
    }

    private void loadUserRoles() {
        ArrayList<String> userRoleNames = new ArrayList<>(Arrays.asList("ADMIN", "MANAGER", "NON_MANAGER"));
        List<UserRole> userRoles = userRoleRepository.findAllByName(userRoleNames);
        userRoles.forEach((userRole) -> {
            userRoleNames.remove(userRole.getName());
        });

        List<UserRole> userRolesToAdd = userRoleNames.stream()
                .map((roleName) -> new UserRole(roleName)).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(userRolesToAdd)) {
            userRoleRepository.saveAll(userRolesToAdd);
        }
    }

    private void loadCompanySizes() {
        ArrayList<String> companySizes = new ArrayList<>(Arrays.asList("1-10", "11-15", "16-25", "26-50", "51-75", "76-100", "101-150", "151-200", "251-300", "301-400", "401-500", "501-600", "601-700", "701-800", "801-900", "901-1000", "1001-1100", "1101-1200", "1201-1300", "1301-1400", "1401-1500", "1501-1600", "1601-1700"));
        List<CompanySize> companySizeList = companySizeRepository.findAllByName(companySizes);
        companySizeList.forEach((companySize) -> {
            companySizes.remove(companySize.getName());
        });

        List<CompanySize> companySizesToAdd = companySizes.stream()
                .map((companySize) -> new CompanySize(companySize)).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(companySizesToAdd)) {
            companySizeRepository.saveAll(companySizesToAdd);
        }
    }
}
