package project.coca.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.coca.auth.jwt.JwtService;
import project.coca.auth.jwt.RefreshTokenDto;
import project.coca.auth.jwt.TokenDto;
import project.coca.common.ApiResponse;
import project.coca.common.error.ErrorCode;
import project.coca.common.success.ResponseCode;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/jwt")
public class JwtController {
    private final JwtService jwtService;

    @PostMapping("/reissue")
    public ApiResponse<TokenDto> reissue(@RequestHeader("Authorization") String accessToken,
                                         @RequestBody RefreshTokenDto refreshToken,
                                         HttpServletRequest request) {
        log.info("reissue token");
        accessToken = accessToken.substring(7);
        try {
            return ApiResponse.response(ResponseCode.OK, jwtService.reissueToken(accessToken, refreshToken.getRefreshToken(), request));
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, "임시 오류 처리, 에러 로그 알려주세요.");
        }
    }

}
