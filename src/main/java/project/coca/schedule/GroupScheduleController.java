package project.coca.schedule;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.coca.auth.jwt.JwtRepository;
import project.coca.auth.jwt.JwtTokenProvider;
import project.coca.auth.jwt.UserSession;
import project.coca.common.ApiResponse;
import project.coca.common.error.ErrorCode;
import project.coca.common.success.ResponseCode;
import project.coca.schedule.request.BringMyScheduleRequest;
import project.coca.schedule.request.GroupScheduleRequest;
import project.coca.schedule.request.HeartRequest;
import project.coca.schedule.response.GroupScheduleResponse;
import project.coca.schedule.response.GroupScheduleSummaryResponse;
import project.coca.schedule.response.PersonalScheduleResponse;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/group-schedule")
public class GroupScheduleController {
    private final GroupScheduleService groupScheduleService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRepository jwtRepository;

    /**
     * 그룹 일정 목록 조회
     *
     * @param groupId   조회 할 그룹 id
     * @param startDate 예시 : 2024-05-01
     * @param endDate   예시 : 2024-05-31
     * @return ApiResponse
     */
    @GetMapping("/summary")
    public ApiResponse<List<GroupScheduleSummaryResponse>> groupScheduleSummaryReq(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam Long groupId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        UserSession session = jwtRepository.getSession(jwtTokenProvider.resolveToken(bearerToken));
        try {
            List<GroupScheduleSummaryResponse> groupScheduleList = groupScheduleService.groupScheduleInquiry(groupId, session.getUsername(), startDate, endDate)
                    .stream()
                    .map(GroupScheduleSummaryResponse::of)
                    .collect(Collectors.toList());

            return ApiResponse.response(ResponseCode.OK, groupScheduleList);
        } catch (NoSuchElementException e) {
            // RequestParam 데이터로 검색되지 않은 데이터가 존재할 경우
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 그룹 일정 상세 정보 조회
     *
     * @param groupId 조회 할 그룹 id
     * @param date    예시 : 2024-05-01
     * @return ApiResponse
     */
    @GetMapping("/detail")
    public ApiResponse<List<GroupScheduleResponse>> detail(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam Long groupId, @RequestParam LocalDate date) {
        UserSession session = jwtRepository.getSession(jwtTokenProvider.resolveToken(bearerToken));
        try {
            List<GroupScheduleResponse> groupScheduleList = groupScheduleService.groupScheduleInquiry(groupId, session.getUsername(), date, date)
                    .stream().map(GroupScheduleResponse::of).collect(Collectors.toList());

            return ApiResponse.response(ResponseCode.OK, groupScheduleList);
        } catch (NoSuchElementException e) {
            // RequestParam 데이터로 검색되지 않은 데이터가 존재할 경우
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 그룹 일정 등록
     *
     * @return ApiResponse
     * @body requestSchedule
     */
    @PostMapping(value = "/add", consumes = "multipart/form-data")
    public ApiResponse<GroupScheduleResponse> groupScheduleRegistrationReq(
            @RequestHeader("Authorization") String bearerToken,
            @RequestPart("data") GroupScheduleRequest requestSchedule,
            @RequestPart(value = "attachments", required = false) MultipartFile[] files) {
        UserSession session = jwtRepository.getSession(jwtTokenProvider.resolveToken(bearerToken));
        try {
            GroupScheduleResponse registGroupSchedule = GroupScheduleResponse.of(groupScheduleService.groupScheduleRegistrationReq(session.getUsername(), requestSchedule, files));

            return ApiResponse.response(ResponseCode.OK, registGroupSchedule);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 그룹 일정 수정
     *
     * @return ApiResponse
     * @body requestSchedule, scheduleId
     */
    @PutMapping(value = "/update", consumes = "multipart/form-data")
    public ApiResponse<GroupScheduleResponse> groupScheduleUpdateReq(
            @RequestHeader("Authorization") String bearerToken,
            @RequestPart("data") GroupScheduleRequest requestSchedule,
            @RequestPart(value = "attachments", required = false) MultipartFile[] files) {
        UserSession session = jwtRepository.getSession(jwtTokenProvider.resolveToken(bearerToken));
        try {
            GroupScheduleResponse updateGroupSchedule = GroupScheduleResponse.of(groupScheduleService.groupScheduleUpdate(session.getUsername(), requestSchedule, files));

            return ApiResponse.response(ResponseCode.OK, updateGroupSchedule);
        } catch (NoSuchAlgorithmException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 그룹 일정 삭제
     *
     * @param groupId    조회 할 그룹 id
     * @param scheduleId 삭제 할 스케쥴 id
     * @return ApiResponse
     */
    @DeleteMapping("/delete")
    public ApiResponse<Boolean> groupScheduleDeleteReq(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam Long groupId, @RequestParam Long scheduleId) {
        UserSession session = jwtRepository.getSession(jwtTokenProvider.resolveToken(bearerToken));
        try {
            boolean result = groupScheduleService.groupScheduleDelete(groupId, scheduleId, session.getUsername());
            return ApiResponse.response(ResponseCode.OK, result);
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 개인 일정으로 저장하기(하트 기능)
     *
     * @param request 조회 할 그룹 id, 저장 할 스케쥴 id, 회원 개인 id
     * @return ApiResponse
     */
    @PostMapping("/heart")
    public ApiResponse<PersonalScheduleResponse> setGroupScheduleToPersonalScheduleReq(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody HeartRequest request) {
        UserSession session = jwtRepository.getSession(jwtTokenProvider.resolveToken(bearerToken));
        try {
            PersonalScheduleResponse result = PersonalScheduleResponse.of(
                    groupScheduleService.setGroupScheduleToPersonalSchedule(
                            request.getGroupId(),
                            request.getScheduleId(),
                            session.getUsername()
                    )
            );
            return ApiResponse.response(ResponseCode.OK, result);
        } catch (NoSuchElementException e) {
            // RequestParam 데이터로 검색되지 않은 데이터가 존재할 경우
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 개인 일정 가져오기
     *
     * @param request - groupId 조회 할 그룹 id, memberId 회원 개인 id, date 선택한 날짜
     * @return ApiResponse
     */
    @PostMapping("/bringMySchedule")
    public ApiResponse<List<GroupScheduleResponse>> bringMySchedule(
            @RequestHeader("Authorization") String bearerToken,
            @RequestBody BringMyScheduleRequest request) {
        UserSession session = jwtRepository.getSession(jwtTokenProvider.resolveToken(bearerToken));
        try {
            List<GroupScheduleResponse> result = groupScheduleService.setPersonalScheduleToGroupSchedule(request.getGroupId(), session.getUsername(), request.getDate())
                    .stream().map(GroupScheduleResponse::of).collect(Collectors.toList());

            return ApiResponse.response(ResponseCode.OK, result);
        } catch (NoSuchElementException e) {
            // RequestParam 데이터로 검색되지 않은 데이터가 존재할 경우
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
