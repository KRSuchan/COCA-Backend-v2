package project.coca.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtTokenProvider jwtTokenProvider;

    public JwtService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenDto reissueToken(String refreshToken, HttpServletRequest request) {
        // Refresh Token 검증
        jwtTokenProvider.validateToken(refreshToken, request);

        // Access Token 에서 User num을 가져옴
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        // 토큰 재발행
        TokenDto tokenDto = new TokenDto(
                jwtTokenProvider.createAccessToken(authentication),
                jwtTokenProvider.createRefreshToken(authentication.getName())
        );

        return tokenDto;
    }
}
