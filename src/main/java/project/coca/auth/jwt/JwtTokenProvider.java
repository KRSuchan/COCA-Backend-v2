package project.coca.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * JWT 생성/파싱/검증 전담 class
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final Key key;
    private final JwtProperties properties;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        byte[] keyBytes = Base64.getDecoder().decode(properties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT AccessToken 생성
     *
     * @param username
     * @return String : AccessToken
     */
    public String createAccessToken(String username) {
        return createToken(username, properties.getAccessExpirationTime());
    }

    /**
     * JWT RefreshToken 생성
     *
     * @param username
     * @return String : RefreshToken
     */
    public String createRefreshToken(String username) {
        return createToken(username, properties.getRefreshExpirationTime());
    }

    /**
     * JWT 생성
     *
     * @param subject
     * @param duration
     * @return
     */
    private String createToken(String subject, long duration) {
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

    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
