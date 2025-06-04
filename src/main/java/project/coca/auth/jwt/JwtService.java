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
    private final JwtRedisService redisService;
    private final MemberRepository memberRepository;

    public TokenDto reissueToken(String accessToken, String refreshToken, HttpServletRequest request) {
        try {
            // Refresh Token 검증
            if (!jwtTokenProvider.validateToken(refreshToken, request)) {
                throw new IllegalArgumentException("Invalid Refresh Token");
            }
            // Refresh Token 에서 username을 가져옴
            String username = redisService.getUsername(refreshToken);
            // accessToken 갱신 : member DB 탐색 -> createAccessToken(username, roles)
            redisService.deleteValue(accessToken);
            redisService.deleteValue(refreshToken);
            Member member = memberRepository.findById(username).orElseThrow();
            String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), Collections.singletonList(member.getRole()));
            UserSession session = redisService.getSession(newAccessToken);
            log.info("New Access Token ! : {}", newAccessToken);
            log.info("test new access token : {}", session.getUsername());
            // refreshToken 갱신
            String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());
            log.info("New Refresh Token ! : {}", newRefreshToken);
            // 반환
            return new TokenDto(
                    newAccessToken,
                    newRefreshToken
            );
        } catch (Exception e) {
            log.error("Token reissue failed: {}", e.getMessage());
            throw new IllegalArgumentException("Token 재발급 실패", e);
        }
    }
}
