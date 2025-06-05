package project.coca.auth.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.coca.domain.personal.Member;
import project.coca.member.MemberRepository;

import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;
    private final MemberRepository memberRepository;
    private final JwtProperties properties;

    public TokenDto reissueToken(String accessToken, String refreshToken, HttpServletRequest request) {
        try {
            // Refresh Token 검증
            if (!jwtTokenProvider.validateToken(refreshToken, request)) {
                throw new IllegalArgumentException("Invalid Refresh Token");
            }
            // Refresh Token 에서 username을 가져옴
            String username = jwtRepository.getUsername(refreshToken);
            Member member = memberRepository.findById(username).orElseThrow();
            username = member.getId();
            jwtRepository.deleteValue(accessToken);
            jwtRepository.deleteValue(refreshToken);
            // token 생성
            String newAccessToken = jwtTokenProvider.createAccessToken(username);
            String newRefreshToken = jwtTokenProvider.createRefreshToken(username);
            // token 저장
            jwtRepository.setValue(accessToken,
                    new UserSession(username, Collections.singletonList(member.getRole())),
                    properties.getAccessExpirationTime());
            jwtRepository.setValue(refreshToken, username, properties.getRefreshExpirationTime());

            log.info("New Access Token : {}", newAccessToken);
            log.info("New Refresh Token : {}", newRefreshToken);
            // 반환
            return new TokenDto(newAccessToken, newRefreshToken);
        } catch (Exception e) {
            log.error("Token reissue failed: {}", e.getMessage());
            throw new IllegalArgumentException("Token 재발급 실패", e);
        }
    }
}
