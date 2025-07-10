package project.coca.schedule;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.coca.auth.jwt.CustomUserDetails;
import project.coca.common.ApiResponse;
import project.coca.common.success.ResponseCode;
import project.coca.domain.personal.PersonalSchedule;
import project.coca.schedule.request.PersonalScheduleRequest;
import project.coca.schedule.response.PersonalScheduleResponse;
import project.coca.schedule.response.PersonalScheduleSummaryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/personal-schedule")
public class PersonalScheduleController {
    private final PersonalScheduleService personalScheduleService;

    /**
     * 개인 일정 등록
     *
     * @param customUserDetails JWT에서 추출한 userDetails
     * @param request           등록할 개인 일정
     * @param attachments       일정에 추가한 첨부파일
     * @return added PersonalSchedule
     */
    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ApiResponse<PersonalScheduleResponse> add(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestPart("data") PersonalScheduleRequest request,
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments) {
        String username = customUserDetails.getUsername();
        log.info("add Personal Schedule's username : {}", username);

        PersonalSchedule savedSchedule = personalScheduleService.add(username, request, attachments);
        PersonalScheduleResponse data = PersonalScheduleResponse.of(savedSchedule);

        return ApiResponse.success(ResponseCode.CREATED, "개인 일정 등록 성공", data);
    }

    /**
     * 개인 일정 목록 조회 (요약 정보)
     *
     * @param customUserDetails JWT에서 추출한 userDetails
     * @param startDate         시작 일자 범위
     * @param endDate           끝 일자 범위
     * @return List<PersonalScheduleSummary> 일정 범위로 조회된 개인 일정 목록의 요약 정보
     */
    @GetMapping("/summary/between-dates")
    public ApiResponse<List<PersonalScheduleSummaryResponse>> getSummaryList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        String username = customUserDetails.getUsername();
        List<PersonalSchedule> schedules = personalScheduleService.findByDates(username, startDate, endDate);
        log.info("""
                Start date: {}
                End date: {}
                username : {}
                found schedule cnt : {}
                """, startDate, endDate, username, schedules.size());
        for (PersonalSchedule schedule : schedules) {
            log.info("color : {}", schedule.getColor());
        }
        List<PersonalScheduleSummaryResponse> data = schedules
                .stream()
                .map(PersonalScheduleSummaryResponse::of)
                .collect(Collectors.toList());
        return ApiResponse.response(ResponseCode.OK, data);
    }

    /**
     * 개인 일정 상세 정보 조회
     *
     * @param customUserDetails JWT에서 추출한 userDetails
     * @param date              일정 상세를 조회할 상세 일자
     * @return List<PersonalSchedule> 상세 일자로 조회된 상세 일정 리스트
     */
    @GetMapping("/detail")
    public ApiResponse<List<PersonalScheduleResponse>> getDetails(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam LocalDate date) {
        String username = customUserDetails.getUsername();

        log.info("Get personal schedules by dates: {}", date);
        log.info("{}", username);

        List<PersonalSchedule> schedules = personalScheduleService.findByDates(username, date, date);
        List<PersonalScheduleResponse> data = schedules
                .stream()
                .map(PersonalScheduleResponse::of)
                .collect(Collectors.toList());
        return ApiResponse.response(ResponseCode.OK, data);
    }

    /**
     * 개인 일정 수정
     *
     * @param customUserDetails JWT에서 추출한 userDetails
     * @param request           수정할 개인 일정
     * @param attachments       일정에 추가한 첨부파일
     * @return : updated PersonalSchedule if not Exception
     * NOT_FOUND : memberId 혹은 scheduleId 로 조회가 되지 않는 경우
     */
    @PutMapping(value = "/update", consumes = {"multipart/form-data"})
    public ApiResponse<PersonalScheduleResponse> update(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @Valid @RequestPart("data") PersonalScheduleRequest request,
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments) {
        String username = customUserDetails.getUsername();
        PersonalSchedule savedSchedule = personalScheduleService.update(username, request, attachments);
        PersonalScheduleResponse data = PersonalScheduleResponse.of(savedSchedule);
        return ApiResponse.success(ResponseCode.OK, "개인 일정 수정 성공", data);
    }

    /**
     * 개인 일정 삭제
     *
     * @param customUserDetails  JWT에서 추출한 userDetails
     * @param personalScheduleId 삭제할 일정 id
     * @return ApiResponse
     * NOT_FOUND : memberId 혹은 personalScheduleId 로 회원이 조회되지 않는 경우
     * OK : 삭제 완료
     */
    @DeleteMapping("/delete")
    public ApiResponse<?> delete(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam Long personalScheduleId) {
        String username = customUserDetails.getUsername();
        personalScheduleService.deleteById(username, personalScheduleId);
        return ApiResponse.success(ResponseCode.OK, "삭제 성공");
    }
}
