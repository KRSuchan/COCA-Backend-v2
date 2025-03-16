package project.coca.v1.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.coca.v1.dto.request.GroupScheduleRequest;
import project.coca.v1.dto.response.GroupSchedule.GroupScheduleResponse;
import project.coca.v1.dto.response.GroupSchedule.GroupScheduleSummaryResponse;
import project.coca.v1.dto.response.common.ApiResponse;
import project.coca.v1.dto.response.common.error.ErrorCode;
import project.coca.v1.dto.response.common.success.ResponseCode;
import project.coca.v1.dto.response.personalSchedule.PersonalScheduleResponse;
import project.coca.v1.service.GroupScheduleService;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/api/group-schedule")
public class GroupScheduleController {
    private final GroupScheduleService groupScheduleService;

    /**
     * 그룹 일정 목록 조회
     *
     * @param groupId   조회 할 그룹 id
     * @param memberId  회원 개인 id
     * @param startDate 예시 : 2024-05-01
     * @param endDate   예시 : 2024-05-31
     * @return ApiResponse
     */
    @GetMapping("/groupScheduleSummaryReq")
    public ApiResponse<List<GroupScheduleSummaryResponse>> groupScheduleSummaryReq(
            @RequestParam Long groupId, @RequestParam String memberId, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        try {
            List<GroupScheduleSummaryResponse> groupScheduleList = groupScheduleService.groupScheduleInquiry(groupId, memberId, startDate, endDate)
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
     * @param groupId     조회 할 그룹 id
     * @param memberId    회원 개인 id
     * @param inquiryDate 예시 : 2024-05-01
     * @return ApiResponse
     */
    @GetMapping("/groupScheduleSpecificReq")
    public ApiResponse<List<GroupScheduleResponse>> groupScheduleSpecificReq(
            @RequestParam Long groupId, @RequestParam String memberId, @RequestParam LocalDate inquiryDate) {
        try {
            List<GroupScheduleResponse> groupScheduleList = groupScheduleService.groupScheduleInquiry(groupId, memberId, inquiryDate, inquiryDate)
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
    @PostMapping(value = "/groupScheduleRegistrationReq", consumes = "multipart/form-data")
    public ApiResponse<GroupScheduleResponse> groupScheduleRegistrationReq(
            @RequestPart("scheduleData") GroupScheduleRequest requestSchedule,
            @RequestPart(value = "scheduleFiles", required = false) MultipartFile[] files) {
        try {
            GroupScheduleResponse registGroupSchedule = GroupScheduleResponse.of(groupScheduleService.groupScheduleRegistrationReq(requestSchedule, files));

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
    @PostMapping(value = "/groupScheduleUpdateReq", consumes = "multipart/form-data")
    public ApiResponse<GroupScheduleResponse> groupScheduleUpdateReq(
            @RequestPart("scheduleData") GroupScheduleRequest requestSchedule,
            @RequestPart(value = "scheduleFiles", required = false) MultipartFile[] files) {
        try {
            GroupScheduleResponse updateGroupSchedule = GroupScheduleResponse.of(groupScheduleService.groupScheduleUpdate(requestSchedule, files));

            return ApiResponse.response(ResponseCode.OK, updateGroupSchedule);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 그룹 일정 삭제
     *
     * @param groupId    조회 할 그룹 id
     * @param scheduleId 삭제 할 스케쥴 id
     * @param memberId   회원 개인 id
     * @return ApiResponse
     */
    @GetMapping("/groupScheduleDeleteReq")
    public ApiResponse<Boolean> groupScheduleDeleteReq(
            @RequestParam Long groupId, @RequestParam Long scheduleId, @RequestParam String memberId) {
        try {
            boolean result = groupScheduleService.groupScheduleDelete(groupId, scheduleId, memberId);

            return ApiResponse.response(ResponseCode.OK, result);
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 개인 일정으로 저장하기(하트 기능)
     *
     * @param groupId    조회 할 그룹 id
     * @param scheduleId 저장 할 스케쥴 id
     * @param memberId   회원 개인 id
     * @return ApiResponse
     */
    @GetMapping("/setGroupScheduleToPersonalScheduleReq")
    public ApiResponse<PersonalScheduleResponse> setGroupScheduleToPersonalScheduleReq(
            @RequestParam Long groupId, @RequestParam Long scheduleId, @RequestParam String memberId) {
        try {
            PersonalScheduleResponse result = PersonalScheduleResponse.of(groupScheduleService.setGroupScheduleToPersonalSchedule(groupId, scheduleId, memberId));

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
     * @param groupId  조회 할 그룹 id
     * @param memberId 회원 개인 id
     * @param date     선택한 날짜
     * @return ApiResponse
     */
    @GetMapping("/setPersonalScheduleToGroupScheduleReq")
    public ApiResponse<List<GroupScheduleResponse>> setPersonalScheduleToGroupScheduleReq(
            @RequestParam Long groupId, @RequestParam String memberId, @RequestParam LocalDate date) {
        try {
            List<GroupScheduleResponse> result = groupScheduleService.setPersonalScheduleToGroupSchedule(groupId, memberId, date)
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
