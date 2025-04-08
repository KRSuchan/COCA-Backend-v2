package project.coca.schedule;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.coca.common.ApiResponse;
import project.coca.common.error.ErrorCode;
import project.coca.common.success.ResponseCode;
import project.coca.schedule.request.FindEmptyScheduleRequest;
import project.coca.schedule.response.CommonSchedule;
import project.coca.schedule.response.PersonalScheduleForEmptyScheduleResponse;
import project.coca.schedule.FindingAlgorithm.CommonScheduleService;

import java.util.List;
import java.util.NoSuchElementException;

@AllArgsConstructor
@RestController
@RequestMapping("/api/commonscheduleController")
public class CommonScheduleController {

    private final CommonScheduleService commonScheduleService;

    @PostMapping("/findEmptyScheduleReq")
    public ApiResponse<List<CommonSchedule>> findEmptyScheduleReq(@RequestBody FindEmptyScheduleRequest request) {
        try {
            List<CommonSchedule> result = commonScheduleService.findEmptySchedule(request);

            return ApiResponse.response(ResponseCode.OK, result);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "찾을 일정의 시간/날을 입력하지 않았습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @PostMapping("/memberScheduleReq")
    public ApiResponse<List<PersonalScheduleForEmptyScheduleResponse>> memberScheduleReq(@RequestBody FindEmptyScheduleRequest memberList) {
        try {
            List<PersonalScheduleForEmptyScheduleResponse> result = commonScheduleService.memberScheduleReq(memberList);
            return ApiResponse.response(ResponseCode.OK, result);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, "조회되지 않는 데이터가 포함되어있습니다.");
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
