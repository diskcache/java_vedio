package com.example.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;

import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

@Data
@Configuration
public class JwtConfig {
    private String secret="GhRrmb732NdA7AigOy6lOWVWrGUQ7CnQFvMlWzD9AxQNapzoWw7jUjizuiPHb6mz"; // 加密密钥
    private long expire=7200; // token 有效时长
    private String header="Authorization"; // header 名称

    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String subject) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(subject)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims getTokenClaim(String token) {
        try {
            // return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenExpired(Date expirationTime) {
        return expirationTime.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getTokenClaim(token).getExpiration();
    }

    public String getUsernameFromToken(String token) {
        return getTokenClaim(token).getSubject();
    }

    public Date getIssuedAtDateFromToken(String token) {
        return getTokenClaim(token).getIssuedAt();
    }
}
