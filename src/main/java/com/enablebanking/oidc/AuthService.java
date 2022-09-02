package com.enablebanking.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ServiceConfiguration serviceConfiguration;

    @Value("classpath:certs/private_key.der")
    private Resource privateKey;

    @Value("classpath:certs/keys.jwks")
    private Resource jwks;

    public boolean isClientIdValid(String clientId) {
        return serviceConfiguration.getClientId().equals(clientId);
    }

    public boolean isRedirectUriValid(String redirectUri) {
        return serviceConfiguration.getRedirectUri().equals(redirectUri);
    }

    @SneakyThrows
    private PrivateKey getPrivateKey() {
        byte[] key = FileCopyUtils.copyToByteArray(privateKey.getInputStream());
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key);
        return kf.generatePrivate(keySpec);
    }

    @SneakyThrows
    private String getMd5Hash(String input) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest((input + serviceConfiguration.getSalt()).getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);
        }
        return sb.toString();
    }

    @SneakyThrows
    public Claims getClaims(String nonce, Boolean isPsd2, String psuId) {
        Claims claims = Jwts.claims();
        claims.put("iss", serviceConfiguration.getIssuer());
        claims.put("sub", getMd5Hash(psuId));
        claims.put("aud", serviceConfiguration.getClientId());
        Date now = new Date();
        claims.setIssuedAt(now);
        Date expiresAt = new Date(now.getTime() + 5 * 60 * 1000);
        claims.setExpiration(expiresAt);
        claims.put("nonce", nonce);
        if (isPsd2) {
            claims.put("psu", psuId);
        }
        return claims;
    }

    @SneakyThrows
    public JWKS getJwks() {
        Reader reader = new InputStreamReader(jwks.getInputStream(), UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(FileCopyUtils.copyToString(reader), JWKS.class);
    }

    @SneakyThrows
    private String getKid() {
        JWKS jwks = getJwks();
        return jwks.getKeys()[0].getKid();
    }

    @SneakyThrows
    public String generateIdToken(String nonce, Boolean isPsd2, String psuId) {
        PrivateKey privateKey = getPrivateKey();
        return Jwts
                .builder()
                .setHeaderParam("kid", getKid())
                .setClaims(getClaims(nonce, isPsd2, psuId))
                .signWith(privateKey, SignatureAlgorithm.PS256)
                .compact();
    }
}
