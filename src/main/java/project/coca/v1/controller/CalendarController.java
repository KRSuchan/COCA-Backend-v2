package project.coca.v1.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.coca.domain.group.CoGroup;
import project.coca.domain.personal.Member;
import project.coca.v1.dto.response.common.ApiResponse;
import project.coca.v1.dto.response.common.error.ErrorCode;
import project.coca.v1.dto.response.common.success.ResponseCode;
import project.coca.v1.dto.response.group.CalendarResponse;
import project.coca.v1.service.GroupService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarController {
    private final GroupService groupService;

    /**
     * 07. 캘린더 목록 조회 (참가중인 그룹 목록 조회)
     *
     * @param memberId : 회원 아이디
     * @return Calendars : 그룹 id, 그룹명, 관리자여부, 매니저여부
     */
    @GetMapping("/member/{memberId}")
    public ApiResponse<?> getMemberId(@PathVariable String memberId) {
        log.info("getMemberId {}", memberId);
        try {
            List<CoGroup> groups = groupService.findJoinedGroups(memberId);
            Member member = new Member();
            member.setId(memberId);
            List<CalendarResponse> data = groups
                    .stream()
                    .map(group -> CalendarResponse.of(group, member))
                    .collect(Collectors.toList());
            return ApiResponse.response(ResponseCode.OK, data);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
