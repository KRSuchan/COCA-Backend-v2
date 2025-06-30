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
import project.coca.domain.personal.PersonalSchedule;
import project.coca.schedule.request.PersonalScheduleRequest;
import project.coca.schedule.response.PersonalScheduleResponse;
import project.coca.schedule.response.PersonalScheduleSummaryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/personal-schedule")
public class PersonalScheduleController {
    private final PersonalScheduleService personalScheduleService;
    private final JwtRepository jwtRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 09. 개인 일정 등록
     *
     * @param request 등록할 일정, 회원 id, 일정의 첨부파일
     * @return ApiResponse
     * NOT_FOUND: memberId로 회원이 조회되지 않는경우
     * CREATED: 그 외 정상 등록한 일정 반환
     */
    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    private ApiResponse<PersonalScheduleResponse> addPersonalSchedule(
            @RequestHeader("Authorization") String bearerToken,
            @RequestPart("data") PersonalScheduleRequest request,
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments) {
        PersonalSchedule personalSchedule = request.getPersonalSchedule();
        String accessToken = jwtTokenProvider.resolveToken(bearerToken);
        UserSession session = jwtRepository.getSession(accessToken);
        try {
            PersonalSchedule savedSchedule = personalScheduleService.savePersonalSchedule(session.getUsername(), personalSchedule, attachments);
            PersonalScheduleResponse data = PersonalScheduleResponse.of(savedSchedule);
            return ApiResponse.success(ResponseCode.CREATED, "개인 일정 등록 성공", data);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 10. 개인 일정 목록 조회 (요약 정보)
     *
     * @param startDate 예시 : 2024-05-01
     * @param endDate   예시 : 2024-05-31
     * @return ApiResponse
     * NOT_FOUND: memberId로 회원이 조회되지 않는경우
     * CREATED: 그 외 정상, 해당 기간 존재하는 일정 반환
     */
    @GetMapping("/summary/between-dates")
    private ApiResponse<List<PersonalScheduleSummaryResponse>> getPersonalScheduleSummaryList(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Start date: {}", startDate);
        log.info("End date: {}", endDate);
        String accessToken = jwtTokenProvider.resolveToken(bearerToken);
        UserSession session = jwtRepository.getSession(accessToken);
        try {
            List<PersonalSchedule> schedules =
                    personalScheduleService.findPersonalSchedulesByDates(session.getUsername(), startDate, endDate);
            List<PersonalScheduleSummaryResponse> data = schedules
                    .stream()
                    .map(PersonalScheduleSummaryResponse::of)
                    .collect(Collectors.toList());
            return ApiResponse.response(ResponseCode.OK, data);
        } catch (NoSuchElementException e) {
            // RequestParam 데이터로 검색되지 않은 데이터가 존재할 경우
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어 있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 11. 개인 일정 상세 정보 조회
     * (FrontEnd 요청사항으로 LIST 반환)
     *
     * @param date 예시 : 2024-05-01
     * @return ApiResponse
     * NOT_FOUND: memberId로 회원이 조회되지 않는 경우
     * CREATED: 그 외 정상, 해당 기간 존재하는 일정 반환
     */
    @GetMapping("/detail")
    public ApiResponse<List<PersonalScheduleResponse>> detail(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam LocalDate date) {
        log.info("Get personal schedules by dates: {}", date);
        String accessToken = jwtTokenProvider.resolveToken(bearerToken);
        UserSession session = jwtRepository.getSession(accessToken);
        try {
            List<PersonalSchedule> schedules =
                    personalScheduleService.findPersonalSchedulesByDates(session.getUsername(), date, date);
            List<PersonalScheduleResponse> data = schedules
                    .stream()
                    .map(PersonalScheduleResponse::of)
                    .collect(Collectors.toList());
            return ApiResponse.response(ResponseCode.OK, data);
        } catch (NoSuchElementException e) {
            // RequestParam 데이터로 검색되지 않은 데이터가 존재할 경우
            return ApiResponse.fail(ErrorCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 12. 개인 일정 수정
     *
     * @param request : 수정할 개인 일정 내용
     * @return : 수정된 개인 일정 내용
     * NOT_FOUND : memberId 혹은 scheduleId 로 조회가 되지 않는 경우
     */
    @PutMapping("/update")
    private ApiResponse<PersonalScheduleResponse> updatePersonalSchedule(
            @RequestHeader("Authorization") String bearerToken,
            @RequestPart("data") PersonalScheduleRequest request,
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments) {
        PersonalSchedule personalSchedule = request.getPersonalSchedule();
        String accessToken = jwtTokenProvider.resolveToken(bearerToken);
        UserSession session = jwtRepository.getSession(accessToken);
        log.info("Update personal schedule: {}", personalSchedule);
        try {
            PersonalSchedule savedSchedule = personalScheduleService.updatePersonalSchedule(session.getUsername(), personalSchedule, attachments);
            PersonalScheduleResponse data = PersonalScheduleResponse.of(savedSchedule);
            return ApiResponse.success(ResponseCode.OK, "개인 일정 수정 성공", data);
        } catch (NoSuchElementException e) {
            // RequestParam 데이터로 검색되지 않은 데이터가 존재할 경우
            return ApiResponse.fail(ErrorCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 13. 개인 일정 삭제
     *
     * @param personalScheduleId : 삭제할 일정 id
     * @return ApiResponse
     * NOT_FOUND : memberId 혹은 personalScheduleId 로 회원이 조회되지 않는 경우
     * OK : 삭제 완료
     */
    @DeleteMapping("/delete")
    private ApiResponse<?> deletePersonalScheduleById(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam Long personalScheduleId) {
        UserSession session = jwtRepository.getSession(jwtTokenProvider.resolveToken(bearerToken));
        try {
            personalScheduleService.deletePersonalScheduleById(session.getUsername(), personalScheduleId);
            return ApiResponse.success(ResponseCode.OK, "삭제 성공");
        } catch (NoSuchElementException e) {
            // RequestParam 데이터로 검색되지 않은 데이터가 존재할 경우
            return ApiResponse.fail(ErrorCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
