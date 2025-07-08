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
            validateRefreshToken(refreshToken, request);
            Member member = getMemberFromRefreshToken(refreshToken);
            revokeOldTokens(accessToken, refreshToken);
            TokenDto newTokens = generateNewTokens(member);
            storeNewTokens(newTokens, member);
            logTokenReissue(newTokens.getAccessToken());
            return newTokens;
        } catch (Exception e) {
            log.error("Token reissue failed: {}", e.getMessage());
            throw new IllegalArgumentException("Token 재발급 실패", e);
        }
    }

    private void validateRefreshToken(String refreshToken, HttpServletRequest request) {
        if (!jwtTokenProvider.validateToken(refreshToken, request)) {
            throw new IllegalArgumentException("Invalid Refresh Token");
        }
    }

    private Member getMemberFromRefreshToken(String refreshToken) {
        String username = jwtRepository.getUsername(refreshToken);
        return memberRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
    }

    private void revokeOldTokens(String accessToken, String refreshToken) {
        jwtRepository.deleteValue(accessToken);
        jwtRepository.deleteValue(refreshToken);
    }

    private TokenDto generateNewTokens(Member member) {
        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        return new TokenDto(newAccessToken, newRefreshToken);
    }

    private void storeNewTokens(TokenDto tokens, Member member) {
        UserSession userSession = new UserSession(
                member.getId(),
                Collections.singletonList(member.getRole())
        );

        jwtRepository.setValue(tokens.getAccessToken(), userSession,
                properties.getAccessExpirationTime());
        jwtRepository.setValue(tokens.getRefreshToken(), member.getId(),
                properties.getRefreshExpirationTime());
    }

    private void logTokenReissue(String newAccessToken) {
        UserSession session = jwtRepository.getSession(newAccessToken);
        log.info("new session by reissued token: {}", session);
        log.info("New tokens generated successfully");
    }
}
