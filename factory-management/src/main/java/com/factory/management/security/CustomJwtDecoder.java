package com.factory.management.security;

import com.factory.management.dto.request.IntrospectRequest;
import com.factory.management.service.ServiceImpl.AuthService;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    private final AuthService authenticationService;

    @Value("${jwt.signer-key}")
    private String signerKey;

    private NimbusJwtDecoder delegate;

    @Override
    public Jwt decode(String token) throws JwtException {
        boolean valid = authenticationService.introspect(
                IntrospectRequest.builder().token(token).build()).isValid();
        if (!valid) {
            throw new JwtException("Invalid token");
        }

        if (delegate == null) {
            SecretKeySpec key = new SecretKeySpec(signerKey.getBytes(), "HS512");
            delegate = NimbusJwtDecoder.withSecretKey(key)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }
        return delegate.decode(token);
    }
}
