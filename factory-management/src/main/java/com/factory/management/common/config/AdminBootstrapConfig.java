package com.factory.management.common.config;

import com.factory.management.modules.masterdata.entity.Employee;
import com.factory.management.modules.auth.entity.Role;
import com.factory.management.modules.auth.entity.User;
import com.factory.management.modules.masterdata.repository.EmployeeRepository;
import com.factory.management.modules.auth.repository.UserRepository;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapConfig implements ApplicationRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled:false}")
    private boolean enabled;

    @Value("${app.bootstrap-admin.username:admin}")
    private String username;

    @Value("${app.bootstrap-admin.password:}")
    private String password;

    @Value("${app.bootstrap-admin.employee-code:SYSTEM-ADMIN}")
    private String employeeCode;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled || userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        String normalizedUsername = username.trim();
        String normalizedEmployeeCode = employeeCode.trim();
        if (normalizedUsername.isEmpty() || normalizedEmployeeCode.isEmpty()) {
            log.warn("Bo qua bootstrap ADMIN vi username hoac employee code bi trong");
            return;
        }
        if (password.isBlank()) {
            log.warn("Bo qua bootstrap ADMIN vi ADMIN_PASSWORD chua duoc cau hinh");
            return;
        }

        User existingUsername = userRepository.findByUsernameIgnoreCase(normalizedUsername).orElse(null);
        if (existingUsername != null) {
            existingUsername.setRoles(new HashSet<>(existingUsername.getRoles()));
            existingUsername.getRoles().add(Role.ADMIN);
            existingUsername.setEnabled(true);
            existingUsername.setAccountNonLocked(true);
            existingUsername.setTokenVersion(
                    existingUsername.getTokenVersion() == null ? 1L : existingUsername.getTokenVersion() + 1
            );
            log.warn("Da cap role ADMIN cho tai khoan bootstrap hien co: {}", normalizedUsername);
            return;
        }

        Employee employee = employeeRepository.findByCodeIgnoreCase(normalizedEmployeeCode).orElseGet(() ->
                employeeRepository.save(Employee.builder()
                        .code(normalizedEmployeeCode)
                        .fullName("Quan tri he thong")
                        .position("System Administrator")
                        .hireDate(LocalDate.now())
                        .active(true)
                        .team(null)
                        .build())
        );

        userRepository.save(User.builder()
                .employee(employee)
                .username(normalizedUsername)
                .passwordHash(passwordEncoder.encode(password))
                .roles(new HashSet<>(Set.of(Role.ADMIN)))
                .enabled(true)
                .accountNonLocked(true)
                .build());

        log.warn(
                "Da khoi tao tai khoan ADMIN bootstrap '{}'. Hay doi mat khau sau lan dang nhap dau tien.",
                normalizedUsername
        );
    }
}
