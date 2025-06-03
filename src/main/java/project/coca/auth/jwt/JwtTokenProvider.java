package project.coca.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private static final Long DEFAULT_ACCESS_EXPIRATION_TIME = 1000L * 60 * 3; // 3분
    private static final Long DEFAULT_REFRESH_EXPIRATION_TIME = 1000L * 60 * 60 * 3; // 3시간

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtRedisService jwtRedisService;
    private final Key key;
    private final @Lazy UserDetailsService userDetailsService;

    @Autowired
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            RedisTemplate<String, Object> redisTemplate,
                            JwtRedisService jwtRedisService,
                            @Lazy UserDetailsService userDetailsService) {
        this.redisTemplate = redisTemplate;
        this.jwtRedisService = jwtRedisService;
        this.userDetailsService = userDetailsService;

        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid JWT secret key", e);
        }
    }

    public String createAccessToken(String username, List<String> roles) {
        String accessToken = generateToken(username, DEFAULT_ACCESS_EXPIRATION_TIME);
        try {
            // 오류 발생
            jwtRedisService.setValue(accessToken, new UserSession(username, roles), DEFAULT_ACCESS_EXPIRATION_TIME);
        } catch (Exception e) {
            log.error("Redis operation failed: {}", e.getMessage());
        }
        return accessToken;
    }

    public String createRefreshToken(String username) {
        String refreshToken = generateToken(username, DEFAULT_REFRESH_EXPIRATION_TIME);
        try {
            jwtRedisService.setValue(refreshToken, username, DEFAULT_REFRESH_EXPIRATION_TIME);
        } catch (Exception e) {
            log.error("Redis operation failed: {}", e.getMessage());
        }
        return refreshToken;
    }

    private String generateToken(String subject, long duration) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + duration);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, HttpServletRequest request) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            request.setAttribute("exception", "TokenExpired");
        } catch (UnsupportedJwtException e) {
            request.setAttribute("exception", "UnsupportedToken");
        } catch (MalformedJwtException e) {
            request.setAttribute("exception", "MalformedToken");
        } catch (IllegalArgumentException e) {
            request.setAttribute("exception", "IllegalArgument");
        }
        return false;
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
