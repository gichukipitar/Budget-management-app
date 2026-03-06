package com.sirhpitar.budget.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.*;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Configuration
public class JwtRsaConfig {

    @Value("classpath:keys/jwt-private.pem")
    private Resource privateKeyPem;

    @Value("classpath:keys/jwt-public.pem")
    private Resource publicKeyPem;

    /**
     * kid is important for rotations. For dev you can keep it static.
     * In prod, rotate keys and publish multiple keys in JWKS.
     */
    @Bean
    public String jwkKeyId() {
        return "budget-auth-" + UUID.randomUUID();
    }

    @Bean
    public RSAKey rsaJwk(String jwkKeyId) {
        RSAPublicKey publicKey = readPublicKey(publicKeyPem);
        RSAPrivateKey privateKey = readPrivateKey(privateKeyPem);

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(jwkKeyId)
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaJwk) {
        JWKSet jwkSet = new JWKSet(rsaJwk);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * Auth service can also validate its own tokens locally using its public key.
     * (Other services will use the JWKS endpoint instead.)
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder(RSAKey rsaJwk) throws JOSEException {
        return NimbusReactiveJwtDecoder.withPublicKey(rsaJwk.toRSAPublicKey()).build();
    }

    // ---------------- PEM parsing helpers ----------------

    private static RSAPrivateKey readPrivateKey(Resource pem) {
        try (InputStream is = pem.getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA private key", e);
        }
    }

    private static RSAPublicKey readPublicKey(Resource pem) {
        try (InputStream is = pem.getInputStream()) {
            String key = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }
}