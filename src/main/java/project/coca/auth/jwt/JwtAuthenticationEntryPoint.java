package project.coca.auth.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import project.coca.common.ApiResponse;
import project.coca.common.error.ErrorCode;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    // 인증 실패 시 호출되는 EntryPoint
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws ServletException, IOException {
        String exceptionType = (String) request.getAttribute("exception");

        if (exceptionType == null) {
            exceptionType = "unknown_error"; // 기본값 설정
        }

        ApiResponse<?> apiResponse = switch (exceptionType) {
            case "TokenExpired" -> ApiResponse.fail(ErrorCode.UNAUTHORIZED, "Expired JWT token");
            case "IllegalArgument" -> ApiResponse.fail(ErrorCode.BAD_REQUEST, "Invalid signature");
            case "MalformedToken" -> ApiResponse.fail(ErrorCode.BAD_REQUEST, "JWT token is MalformedToken");
            case "UnsupportedToken" -> ApiResponse.fail(ErrorCode.BAD_REQUEST, "Unsupported JWT token");
            case "invalidJwt" -> ApiResponse.fail(ErrorCode.BAD_REQUEST, "Invalid JWT token");
            case "nullToken" -> ApiResponse.fail(ErrorCode.BAD_REQUEST, "JWT token is missing");
            default -> ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
        };

        log.error("Authentication error occurred: {}, {}", exceptionType, authException.getClass());

        response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 표준 상태 코드 설정
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(apiResponse.toString());
        response.getWriter().flush();
    }
}
