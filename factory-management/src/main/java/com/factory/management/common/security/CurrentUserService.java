package com.factory.management.common.security;

import com.factory.management.modules.masterdata.entity.Employee;
import com.factory.management.modules.auth.entity.User;
import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User user() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthentication))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        return userRepository.findByUsernameIgnoreCase(jwtAuthentication.getToken().getSubject())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Employee employee() { return user().getEmployee(); }
}
