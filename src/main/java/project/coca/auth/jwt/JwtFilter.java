package project.coca.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 헤더(Authorization)에 있는 토큰을 꺼내 이상이 없는 경우 SecurityContext에 저장
 * Request 이전에 작동
 */

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = jwtTokenProvider.resolveToken(request.getHeader("Authorization"));
        log.info("jwt filter, access token :\"{}\"", accessToken);
        try {
            // 토큰이 유효한 경우 SecurityContext에 인증 정보 저장
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken, request)) {
                UserSession session = jwtRepository.getSession(accessToken);
                if (session == null) {
                    log.warn("No session found for token: {}", accessToken);
                    throw new IllegalStateException("Session expired or not found in Redis");
                }
                UserDetails userDetails = new CustomUserDetails(session);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection 실패: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Redis connection 실패\"}");
            return;
        } catch (Exception e) {
            log.error("Unexpected error during JWT processing: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 설정
            response.getWriter().write("Invalid or missing JWT token");
            return; // 요청을 중단
        }

        // 필터 체인 진행
        filterChain.doFilter(request, response);
    }

}