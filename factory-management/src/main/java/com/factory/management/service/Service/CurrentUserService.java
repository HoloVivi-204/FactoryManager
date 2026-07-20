package com.factory.management.service.Service;

import com.factory.management.entity.*;
import com.factory.management.exception.*;
import com.factory.management.repository.*;
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
