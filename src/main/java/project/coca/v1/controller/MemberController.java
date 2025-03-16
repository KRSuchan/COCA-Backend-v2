package project.coca.v1.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.coca.security.jwt.TokenDto;
import project.coca.v1.dto.request.MemberFunctionRequest;
import project.coca.v1.dto.request.MemberJoinRequest;
import project.coca.v1.dto.request.MemberUpdateRequest;
import project.coca.v1.dto.response.Member.MemberResponse;
import project.coca.v1.dto.response.common.ApiResponse;
import project.coca.v1.dto.response.common.error.ErrorCode;
import project.coca.v1.dto.response.common.success.ResponseCode;
import project.coca.v1.dto.response.tag.InterestForTag;
import project.coca.v1.service.MemberService;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.NoSuchElementException;

@ComponentScan
@RequestMapping("/api/member")
@RestController
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/memberProfileImageUrlReq")
    public ApiResponse<String> MemberProfileImageUrlReq(@RequestParam String memberId) {
        try {
            return ApiResponse.response(ResponseCode.OK, memberService.getMemberProfileUrl(memberId));
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 아이디 중복 체크
     * id만 필요
     */
    @PostMapping("/validate-id")
    public ApiResponse<Boolean> checkDuplicationId(@RequestBody MemberUpdateRequest memberRequest) {
        try {
            return ApiResponse.response(ResponseCode.OK, memberService.checkDuplicationId(memberRequest.getId()));
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, "예상 못한 에러, 로그 :" + e.getMessage());
        }
    }

    /**
     * 회원가입
     */
    @PostMapping(value = "/joinReq", consumes = {"multipart/form-data"})
    public ApiResponse<MemberResponse> JoinReq(@RequestPart("data") MemberJoinRequest joinMember,
                                               @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        try {
            MemberResponse joinResult = MemberResponse.of(memberService.memberJoin(joinMember, profileImage));

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
    @PostMapping("/loginReq")
    public ApiResponse<TokenDto> LoginReq(@RequestBody MemberFunctionRequest loginMember) {
        try {
            return ApiResponse.response(ResponseCode.OK, memberService.login(loginMember));
        } catch (NoSuchElementException e) {
            // RequestParam 데이터에 조회되지 않는 데이터 있는 경우. 이 경우에는 아이디 조회 안되는거라 이런 메시지..~
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "동일한 아이디의 회원이 이미 존재합니다.");
        } catch (BadCredentialsException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "아이디(로그인 전용 아이디) 또는 비밀번호를 잘못 입력했습니다.\n입력하신 내용을 다시 확인해주세요.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/checkPassword")
    public ApiResponse<Boolean> memberCheckReq(@RequestBody MemberFunctionRequest loginMember) {
        try {
            return ApiResponse.response(ResponseCode.OK, memberService.memberCheck(loginMember));
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "비밀번호 불일치");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logoutReq")
    public ApiResponse<Boolean> logoutReq() {
        return ApiResponse.response(ResponseCode.OK, memberService.logout());
    }


    /**
     * 회원탈퇴
     *
     * @return ApiResponse
     * @body withdrawalMember 회원탈퇴 할 회원의 정보
     */
    @PostMapping("/withdrawalReq")
    public ApiResponse<Boolean> WithdrawalReq(@RequestBody MemberFunctionRequest withdrawalMember) {
        try {
            //true면 정상 삭제, false면 무언가에 의해 삭제 안됨
            return ApiResponse.response(ResponseCode.OK, memberService.withdrawal(withdrawalMember));
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
     *
     * @return ApiResponse
     * @body inquiryMember 개인정보를 조회 할 회원의 정보
     */
    @PostMapping("/memberInfoInquiryReq")
    public ApiResponse<MemberResponse> MemberInfoInquiryReq(@RequestBody MemberFunctionRequest inquiryMember) {
        try {
            MemberResponse inquiryResult = MemberResponse.of(memberService.memberInfoInquiry(inquiryMember));

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
     *
     * @return ApiResponse
     * @body newInfo 수정 할 회원의 새 정보
     */
    @PostMapping(value = "/memberInfoUpdateReq", consumes = {"multipart/form-data"})
    public ApiResponse<MemberResponse> MemberInfoUpdateReq(@RequestPart("data") MemberUpdateRequest newInfo,
                                                           @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        System.out.println("isNull = " + (profileImage == null));

        try {
            MemberResponse updateResult = MemberResponse.of(memberService.memberInfoUpdate(newInfo, profileImage));
            return ApiResponse.response(ResponseCode.OK, updateResult);
        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping("/memberTagInquiryReq")
    public ApiResponse<List<InterestForTag>> MemberTagInquiryReq(@RequestParam String memberId) {
        try {
            List<InterestForTag> result = memberService.memberTagInquiry(memberId);

            return ApiResponse.response(ResponseCode.OK, result);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
