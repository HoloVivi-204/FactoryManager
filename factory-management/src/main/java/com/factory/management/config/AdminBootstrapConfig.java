package com.factory.management.config;

import com.factory.management.entity.Employee;
import com.factory.management.entity.Role;
import com.factory.management.entity.User;
import com.factory.management.repository.EmployeeRepository;
import com.factory.management.repository.UserRepository;
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

    @Value("${app.bootstrap-admin.enabled:true}")
    private boolean enabled;

    @Value("${app.bootstrap-admin.username:admin}")
    private String username;

    @Value("${app.bootstrap-admin.password:Admin@123456}")
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
        if (normalizedUsername.isEmpty() || password.isBlank() || normalizedEmployeeCode.isEmpty()) {
            throw new IllegalStateException("Thong tin bootstrap Admin khong duoc de trong");
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
