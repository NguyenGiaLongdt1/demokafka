package com.example.demo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    public static final String CLAIM_TOKEN_TYPE = "token_type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${app.jwt.access-secret}")
    private String accessSecret;

    @Value("${app.jwt.access-expiration-ms:900000}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    private SecretKey getAccessSigningKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
    }

    private SecretKey getRefreshSigningKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 1. Sinh Access Token (Thời hạn ngắn: 15 phút, token_type = access, kèm roles)
     */
    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .claim("roles", userDetails.getAuthorities().stream().map(Object::toString).toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpirationMs))
                .signWith(getAccessSigningKey())
                .compact();
    }

    /**
     * Tương thích ngược với code cũ
     */
    public String generateToken(UserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    /**
     * 2. Sinh Refresh Token (Thời hạn dài: 7 ngày, token_type = refresh, ký bằng secret riêng)
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(getRefreshSigningKey())
                .compact();
    }

    public String extractUsernameFromAccessToken(String token) {
        return extractClaimFromAccess(token, Claims::getSubject);
    }

    public String extractUsernameFromRefreshToken(String token) {
        return extractClaimFromRefresh(token, Claims::getSubject);
    }

    public String extractUsername(String token) {
        return extractUsernameFromAccessToken(token);
    }

    /**
     * Kiểm tra tính hợp lệ của Access Token
     */
    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaimsFromAccess(token);
            String username = claims.getSubject();
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            return username.equals(userDetails.getUsername())
                    && TOKEN_TYPE_ACCESS.equals(tokenType)
                    && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return isAccessTokenValid(token, userDetails);
    }

    /**
     * Kiểm tra tính hợp lệ của Refresh Token:
     * - Chữ ký khớp với refreshSecret
     * - Chưa hết hạn (expiration)
     * - Claim token_type phải đúng bằng "refresh"
     * - Khớp username
     */
    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaimsFromRefresh(token);
            String username = claims.getSubject();
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
            return username.equals(userDetails.getUsername())
                    && TOKEN_TYPE_REFRESH.equals(tokenType)
                    && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public <T> T extractClaimFromAccess(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaimsFromAccess(token);
        return claimsResolver.apply(claims);
    }

    public <T> T extractClaimFromRefresh(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaimsFromRefresh(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaimsFromAccess(String token) {
        return Jwts.parser()
                .verifyWith(getAccessSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Claims extractAllClaimsFromRefresh(String token) {
        return Jwts.parser()
                .verifyWith(getRefreshSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return accessExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }
}

