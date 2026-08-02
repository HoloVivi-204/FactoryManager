package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.ChangePasswordRequest;
import com.factory.management.dto.request.IntrospectRequest;
import com.factory.management.dto.request.LoginRequest;
import com.factory.management.dto.request.LogoutRequest;
import com.factory.management.dto.request.RefreshRequest;
import com.factory.management.dto.request.RegisterRequest;
import com.factory.management.dto.response.AuthResponse;
import com.factory.management.dto.response.IntrospectResponse;
import com.factory.management.entity.Employee;
import com.factory.management.entity.InvalidToken;
import com.factory.management.entity.Role;
import com.factory.management.entity.User;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.EmployeeRepository;
import com.factory.management.repository.InvalidTokenRepository;
import com.factory.management.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final InvalidTokenRepository invalidTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.signer-key}")
    private String signerKey;

    @Value("${jwt.valid-duration}")
    private long validDuration;

    @Value("${jwt.refresh-duration}")
    private long refreshDuration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTS);
        }
        if (userRepository.existsByEmployee_Id(request.getEmployeeId())) {
            throw new AppException(ErrorCode.EMPLOYEE_ACCOUNT_EXISTS);
        }

        Employee employee = employeeRepository
                .findByIdAndActiveTrue(request.getEmployeeId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_NOT_FOUND));
        User user = userRepository.save(User.builder()
                .employee(employee)
                .username(request.getUsername().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(new HashSet<>(Set.of(Role.EMPLOYEE)))
                .build());
        return response(user, generateToken(user));
    }

    @Transactional
    public AuthResponse authenticate(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));
        if (!user.getEnabled() || !user.getAccountNonLocked()
                || !Boolean.TRUE.equals(user.getEmployee().getActive())
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        user.setLastLoginAt(LocalDateTime.now());
        return response(user, generateToken(user));
    }

    @Transactional(readOnly = true)
    public IntrospectResponse introspect(IntrospectRequest request) {
        boolean valid;
        try {
            verifyToken(request.getToken(), false);
            valid = true;
        } catch (AppException exception) {
            valid = false;
        }
        return IntrospectResponse.builder().valid(valid).build();
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        SignedJWT signedJWT = verifyToken(request.getToken(), true);
        invalidate(signedJWT);
        try {
            User user = findActiveUser(signedJWT.getJWTClaimsSet().getSubject());
            return response(user, generateToken(user));
        } catch (ParseException exception) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    @Transactional
    public void logout(LogoutRequest request) {
        SignedJWT signedJWT = verifyToken(request.getToken(), true);
        invalidate(signedJWT);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = findActiveUser(username);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash()))
            throw new AppException(ErrorCode.INVALID_CURRENT_PASSWORD);
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash()))
            throw new AppException(ErrorCode.NEW_PASSWORD_MUST_DIFFER);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
    }

    @Transactional(readOnly = true)
    public AuthResponse me(String username) {
        return response(findActiveUser(username), null);
    }

    public SignedJWT verifyToken(String token, boolean refresh) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(signerKey.getBytes(StandardCharsets.UTF_8));
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            Date expiry = refresh
                    ? Date.from(claims.getIssueTime().toInstant().plusMillis(refreshDuration))
                    : claims.getExpirationTime();
            if (!signedJWT.verify(verifier) || expiry == null || !expiry.after(new Date())
                    || invalidTokenRepository.existsById(claims.getJWTID())) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            User user = findActiveUser(claims.getSubject());
            Long tokenVersion = claims.getLongClaim("tokenVersion");
            if (!Objects.equals(tokenVersion, user.getTokenVersion())) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }
            return signedJWT;
        } catch (ParseException | JOSEException exception) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String generateToken(User user) {
        Date now = new Date();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("FactoryManagement")
                .issueTime(now)
                .expirationTime(Date.from(Instant.now().plusMillis(validDuration)))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("userId", user.getId())
                .claim("employeeId", user.getEmployee().getId())
                .claim("tokenVersion", user.getTokenVersion())
                .build();
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS512), new Payload(claims.toJSONObject()));
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes(StandardCharsets.UTF_8)));
            return jwsObject.serialize();
        } catch (JOSEException exception) {
            log.error("Cannot create JWT", exception);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private void invalidate(SignedJWT jwt) {
        try {
            invalidTokenRepository.deleteAllByExpiryTimeBefore(LocalDateTime.now());
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            LocalDateTime refreshExpiry = LocalDateTime.ofInstant(
                    claims.getIssueTime().toInstant().plusMillis(refreshDuration), ZoneId.systemDefault());
            invalidTokenRepository.save(InvalidToken.builder()
                    .id(claims.getJWTID()).expiryTime(refreshExpiry).build());
        } catch (ParseException exception) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    private User findActiveUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!user.getEnabled() || !user.getAccountNonLocked()
                || !Boolean.TRUE.equals(user.getEmployee().getActive())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return user;
    }

    private String buildScope(User user) {
        StringJoiner joiner = new StringJoiner(" ");
        user.getRoles().forEach(role -> joiner.add("ROLE_" + role.name()));
        return joiner.toString();
    }

    private AuthResponse response(User user, String token) {
        return AuthResponse.builder().token(token).authenticated(true)
                .expiresIn(validDuration / 1000).userId(user.getId())
                .employeeId(user.getEmployee().getId()).employeeCode(user.getEmployee().getCode())
                .employeeName(user.getEmployee().getFullName()).username(user.getUsername())
                .roles(Set.copyOf(user.getRoles())).build();
    }
}
