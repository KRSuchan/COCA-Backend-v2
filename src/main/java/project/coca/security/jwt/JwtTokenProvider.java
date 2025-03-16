package project.coca.security.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private static final Long DEFAULT_ACCESS_EXPIRATION_TIME = 1000L * 60 * 60; // 1시간
    private static final Long DEFAULT_REFRESH_EXPIRATION_TIME = 1000L * 60 * 60 * 3; // 3시간

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtRedisService jwtRedisService;
    private final Key key;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            RedisTemplate<String, String> redisTemplate,
                            JwtRedisService jwtRedisService,
                            UserDetailsService userDetailsService) {
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

    public String createAccessToken(Authentication authentication) {
        return generateToken(authentication.getName(), DEFAULT_ACCESS_EXPIRATION_TIME);
    }

    public String createRefreshToken(String username) {
        String refreshToken = generateToken(username, DEFAULT_REFRESH_EXPIRATION_TIME);
        try {
            jwtRedisService.setToken(username, refreshToken, DEFAULT_REFRESH_EXPIRATION_TIME);
        } catch (Exception e) {
            log.error("Redis operation failed: {}", e.getMessage());
        }
        return refreshToken;
    }

    public String generateToken(String subject, long duration) {
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
        } catch (JwtException e) {
            log.info("JWT validation failed: {}", e.getMessage());
            request.setAttribute("exception", e.getClass().getSimpleName());
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        String username = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody().getSubject();

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
