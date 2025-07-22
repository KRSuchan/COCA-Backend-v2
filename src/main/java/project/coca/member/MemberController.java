package project.coca.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.coca.base.ApiResponse;
import project.coca.base.error.ErrorCode;
import project.coca.base.success.ResponseCode;
import project.coca.jwt.JwtTokenProvider;
import project.coca.jwt.dto.RefreshTokenDto;
import project.coca.jwt.dto.TokenDto;
import project.coca.member.dto.MemberJoinRequest;
import project.coca.member.dto.MemberLoginRequest;
import project.coca.member.dto.MemberResponse;
import project.coca.member.dto.MemberUpdateRequest;
import project.coca.tag.dto.InterestForTag;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RequestMapping("/api/member")
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원 프로필 이미지 url 조회
     */
    @GetMapping("/memberProfileImageUrlReq")
    public ApiResponse<String> getProfileImageUrl(@RequestParam String memberId) {
        try {
            return ApiResponse.response(ResponseCode.OK, memberService.readProfileUrl(memberId));
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 아이디 중복 체크
     */
    @PostMapping("/validate-id")
    public ApiResponse<Boolean> checkUsableId(@RequestBody MemberUpdateRequest memberRequest) {
        try {
            return ApiResponse.response(ResponseCode.OK, memberService.isUsable(memberRequest.getId()));
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, "예상 못한 에러, 로그 :" + e.getMessage());
        }
    }

    /**
     * 회원가입
     */
    @PostMapping(value = "/joinReq", consumes = {"multipart/form-data"})
    public ApiResponse<MemberResponse> join(@RequestPart("data") MemberJoinRequest joinMember,
                                            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            MemberResponse joinResult = MemberResponse.of(memberService.joinMember(joinMember, profileImage));
            return ApiResponse.response(ResponseCode.OK, joinResult);
        } catch (DuplicateKeyException e) {
            // RequestParam 데이터와 동일한 아이디의 회원이 있을 경우
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "동일한 아이디의 회원이 이미 존재합니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ApiResponse<TokenDto> login(@RequestBody MemberLoginRequest loginMember) {
        return ApiResponse.response(ResponseCode.OK, memberService.login(loginMember.getId(), loginMember.getPassword()));
    }

    /**
     * 개인정보조회 전 비밀번호 확인
     */
    @PostMapping("/checkPassword")
    public ApiResponse<Boolean> checkAccount(@RequestBody MemberLoginRequest loginMember) {
        return ApiResponse.response(ResponseCode.OK, memberService.checkMember(loginMember.getId(), loginMember.getPassword()));
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody RefreshTokenDto refreshTokenDto) {
        String accessToken = jwtTokenProvider.resolveToken(bearerToken);
        return ApiResponse.response(ResponseCode.OK, memberService.logout(accessToken, refreshTokenDto.getRefreshToken()));
    }


    /**
     * 회원탈퇴
     */
    @PostMapping("/withdrawalReq")
    public ApiResponse<Boolean> closeAccount(@RequestBody MemberLoginRequest withdrawalMember) {
        try {
            //true면 정상 삭제, false면 무언가에 의해 삭제 안됨
            return ApiResponse.response(ResponseCode.OK, memberService.closeAccount(withdrawalMember));
        } catch (NoSuchElementException e) {
            // RequestParam 데이터에 조회되지 않는 데이터 있는 경우
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (AuthenticationException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 개인정보조회
     */
    @PostMapping("/info")
    public ApiResponse<MemberResponse> getAccountInfo(@RequestBody MemberLoginRequest inquiryMember) {
        try {
            MemberResponse inquiryResult = MemberResponse.of(memberService.getMemberInfo(inquiryMember));

            return ApiResponse.response(ResponseCode.OK, inquiryResult);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "회원이 조회되지 않습니다.");
        } catch (AuthenticationException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 개인정보수정
     */
    @PutMapping(value = "/update", consumes = {"multipart/form-data"})
    public ApiResponse<MemberResponse> updateAccountInfo(@RequestPart("data") MemberUpdateRequest newInfo,
                                                         @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        System.out.println("isNull = " + (profileImage == null));

        try {
            MemberResponse updateResult = MemberResponse.of(memberService.updateMemberInfo(newInfo, profileImage));
            return ApiResponse.response(ResponseCode.OK, updateResult);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 회원 관심 태그 조회
     */
    @GetMapping("/memberTagInquiryReq")
    public ApiResponse<List<InterestForTag>> getMemberTagInfo(@RequestParam String memberId) {
        try {
            List<InterestForTag> result = memberService.getMemberTags(memberId);
            return ApiResponse.response(ResponseCode.OK, result);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
