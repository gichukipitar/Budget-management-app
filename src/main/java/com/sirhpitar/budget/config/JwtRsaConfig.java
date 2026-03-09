package com.sirhpitar.budget.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({JwtProps.class, AuthProps.class})
public class JwtRsaConfig {

    private final JwtProps jwtProps;

    @Bean
    public String jwkKeyId() {
        return jwtProps.keyId();
    }

    @Bean
    public RSAKey rsaJwk(String jwkKeyId) {
        RSAPublicKey publicKey = readPublicKey(jwtProps.publicKeyLocation());
        RSAPrivateKey privateKey = readPrivateKey(jwtProps.privateKeyLocation());

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(jwkKeyId)
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaJwk) {
        return new ImmutableJWKSet<>(new JWKSet(rsaJwk));
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(RSAKey rsaJwk) throws JOSEException {
        return NimbusReactiveJwtDecoder.withPublicKey(rsaJwk.toRSAPublicKey()).build();
    }

    private static RSAPrivateKey readPrivateKey(String location) {
        try {
            Resource resource = new DefaultResourceLoader().getResource(location);
            try (InputStream is = resource.getInputStream()) {
                String key = new String(is.readAllBytes())
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s+", "");

                byte[] decoded = Base64.getDecoder().decode(key);
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return (RSAPrivateKey) kf.generatePrivate(spec);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA private key from " + location, e);
        }
    }

    private static RSAPublicKey readPublicKey(String location) {
        try {
            Resource resource = new DefaultResourceLoader().getResource(location);
            try (InputStream is = resource.getInputStream()) {
                String key = new String(is.readAllBytes())
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s+", "");

                byte[] decoded = Base64.getDecoder().decode(key);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                return (RSAPublicKey) kf.generatePublic(spec);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key from " + location, e);
        }
    }
}