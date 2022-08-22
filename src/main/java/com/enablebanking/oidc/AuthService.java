package com.enablebanking.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ServiceConfiguration serviceConfiguration;
    private final String resourcePath = "src/main/resources/";

    public boolean isClientIdValid(String clientId) {
        return serviceConfiguration.getClientId().equals(clientId);
    }

    public boolean isRedirectUriValid(String redirectUri) {
        return serviceConfiguration.getRedirectUri().equals(redirectUri);
    }

    @SneakyThrows
    private static PrivateKey getPrivateKey(String filename) {
        byte[] key = Files.readAllBytes(Paths.get(filename));
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
    private JWKS getJwks() {
        String jwksPath = resourcePath + serviceConfiguration.getJwksPath();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(Files.readAllBytes(Paths.get(jwksPath)), JWKS.class);
    }

    @SneakyThrows
    private String getKid() {
        JWKS jwks = getJwks();
        return jwks.getKeys()[0].getKid();
    }

    @SneakyThrows
    public String generateIdToken(String nonce, Boolean isPsd2, String psuId) {
        PrivateKey privateKey = getPrivateKey(resourcePath + serviceConfiguration.getPrivateKeyPath());
        return Jwts
                .builder()
                .setHeaderParam("kid", getKid())
                .setClaims(getClaims(nonce, isPsd2, psuId))
                .signWith(privateKey, SignatureAlgorithm.PS256)
                .compact();
    }
}
