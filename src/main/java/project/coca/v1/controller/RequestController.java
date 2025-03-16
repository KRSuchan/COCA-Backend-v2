package project.coca.v1.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.coca.domain.request.FriendRequest;
import project.coca.domain.request.GroupRequest;
import project.coca.domain.request.RequestStatus;
import project.coca.domain.request.ScheduleRequest;
import project.coca.v1.dto.request.RequestUpdateRequest;
import project.coca.v1.dto.request.ScheduleRequestRequest;
import project.coca.v1.dto.response.common.ApiResponse;
import project.coca.v1.dto.response.common.error.AlreadyReportedException;
import project.coca.v1.dto.response.common.error.ErrorCode;
import project.coca.v1.dto.response.common.success.ResponseCode;
import project.coca.v1.dto.response.request.FriendRequestResponse;
import project.coca.v1.dto.response.request.GroupRequestResponse;
import project.coca.v1.dto.response.request.ScheduleRequestResponse;
import project.coca.v1.service.RequestService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/request")
public class RequestController {
    private final RequestService requestService;

    /**
     * 36-1. 친구 요청 등록
     */
    @PostMapping("/add/friend/from/{fromId}/to/{toId}")
    public ApiResponse<?> addFriendRequest(@PathVariable String fromId, @PathVariable String toId) {
        log.info("add FriendRequest from {} to {}", fromId, toId);
        if (fromId.equals(toId)) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "from 과 to 가 동일합니다.");
        }
        try {
            requestService.addFriendRequest(fromId, toId);
            return ApiResponse.success(ResponseCode.CREATED, "친구 요청 완료");
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (AlreadyReportedException e) {
            return ApiResponse.fail(ErrorCode.ALREADY_EXISTS, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 36-2. 그룹 초대 요청 등록
     */
    @PostMapping("/add/group-invite/from/{fromId}/to/{toId}/group/{groupId}")
    public ApiResponse<?> addGroupRequest(@PathVariable String fromId, @PathVariable String toId, @PathVariable Long groupId) {
        log.info("add FriendRequest from {} to {} group {}", fromId, toId, groupId);
        if (fromId.equals(toId)) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "from 과 to 가 동일합니다.");
        }
        try {
            requestService.addGroupRequest(fromId, toId, groupId);
            return ApiResponse.success(ResponseCode.CREATED, "그룹 초대 요청 완료");
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (AlreadyReportedException e) {
            return ApiResponse.fail(ErrorCode.ALREADY_EXISTS, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 36-3. 빈일정 추가 요청 등록 통합
     */
    @PostMapping("/add/schedule")
    public ApiResponse<?> addScheduleRequest(@RequestBody ScheduleRequestRequest request) {
        log.info("add schedule {}", request);
        try {
            requestService.addScheduleRequest(request.getSender(), request.getRequestedSchedule(), request.getReceivers());
            return ApiResponse.success(ResponseCode.OK, "빈일정 추가 요청 등록 완료");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    /**
     * 37-1. 친구 요청 목록 조회
     */
    @GetMapping("/list/friend/member/{memberId}")
    public ApiResponse<List<FriendRequestResponse>> findFriendRequests(@PathVariable String memberId) {
        log.info("friend list by member {}", memberId);
        try {
            List<FriendRequest> requests = requestService.findFriendRequests(memberId);
            List<FriendRequestResponse> data = requests
                    .stream()
                    .map(FriendRequestResponse::of)
                    .collect(Collectors.toList());
            return ApiResponse.response(ResponseCode.OK, data);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 37-2. 그룹 초대 요청 목록 조회
     */
    @GetMapping("/list/group-invite/member/{memberId}")
    public ApiResponse<List<GroupRequestResponse>> findGroupRequests(@PathVariable String memberId) {
        log.info("group request list by member {}", memberId);
        try {
            List<GroupRequest> requests = requestService.findGroupRequests(memberId);
            List<GroupRequestResponse> data = requests
                    .stream()
                    .map(GroupRequestResponse::of)
                    .collect(Collectors.toList());
            return ApiResponse.response(ResponseCode.OK, data);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 37-3. 빈일정 추가 요청 목록 조회
     */
    @GetMapping("/list/schedule/member/{memberId}")
    public ApiResponse<List<ScheduleRequestResponse>> findScheduleRequests(@PathVariable String memberId) {
        log.info("schedule request list by member {}", memberId);
        try {
            List<ScheduleRequest> requests = requestService.findScheduleRequests(memberId);
            List<ScheduleRequestResponse> data = requests
                    .stream()
                    .map(ScheduleRequestResponse::of)
                    .collect(Collectors.toList());
            return ApiResponse.response(ResponseCode.OK, data);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 38-1. 친구 요청 수정 => 수락의 경우 친구 등록
     */
    @PutMapping("/update/friend")
    public ApiResponse<?> updateFriendRequest(@RequestBody RequestUpdateRequest request) {
        Long requestId = request.getRequestId();
        RequestStatus status = request.getStatus();
        log.info("update friend request id {} status {}", requestId, status);
        if (status == RequestStatus.PENDING) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "대기 상태로 변경할 수 없습니다.");
        }
        try {
            requestService.updateFriendRequest(requestId, status);
            return ApiResponse.success(ResponseCode.OK, "친구 요청 수정 완료");
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 38-2. 그룹 초대 요청 수정 => 수락의 경우 그룹 참가하기
     */
    @PutMapping("/update/group-invite")
    public ApiResponse<?> updateGroupInviteRequest(@RequestBody RequestUpdateRequest request) {
        Long requestId = request.getRequestId();
        RequestStatus status = request.getStatus();
        log.info("update group request id {} status {}", requestId, status);
        if (status == RequestStatus.PENDING) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "대기 상태로 변경할 수 없습니다.");
        }
        try {
            requestService.updateGroupRequest(requestId, status);
            return ApiResponse.success(ResponseCode.OK, "그룹 초대 요청 수정 완료");
        } catch (AlreadyReportedException e) {
            return ApiResponse.success(ResponseCode.ALREADY_REPORTED, e.getMessage());
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 38-3. 빈일정 추가 요청 수정
     */
    @PutMapping("/update/schedule")
    public ApiResponse<?> updateScheduleRequest(@RequestBody RequestUpdateRequest request) {
        Long requestId = request.getRequestId();
        RequestStatus status = request.getStatus();
        log.info("update schedule request id {} status {}", requestId, status);
        if (status == RequestStatus.PENDING) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, "대기 상태로 변경할 수 없습니다.");
        }
        try {
            requestService.updateScheduleRequest(requestId, status);
            return ApiResponse.success(ResponseCode.OK, "일정 요청 수정 완료");
        } catch (AlreadyReportedException e) {
            return ApiResponse.success(ResponseCode.ALREADY_REPORTED, e.getMessage());
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 39-1. 친구 요청 삭제
     */
    @DeleteMapping("/delete/friend/{requestId}")
    public ApiResponse<?> deleteFriendRequest(@PathVariable Long requestId) {
        log.info("delete friend request id {}", requestId);
        try {
            requestService.deleteFriendRequest(requestId);
            return ApiResponse.success(ResponseCode.OK, "친구 요청 삭제 완료");
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 39-2. 그룹 초대 요청 삭제
     */
    @DeleteMapping("/delete/group-invite/{requestId}")
    public ApiResponse<?> deleteGroupInviteRequest(@PathVariable Long requestId) {
        log.info("delete group invite request id {}", requestId);
        try {
            requestService.deleteGroupRequest(requestId);
            return ApiResponse.success(ResponseCode.OK, "그룹 초대 요청 삭제 완료");
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 39-3. 빈일정 추가 요청 삭제
     */
    @DeleteMapping("/delete/schedule/{requestId}")
    public ApiResponse<?> deleteScheduleRequest(@PathVariable Long requestId) {
        log.info("delete schedule request id {}", requestId);
        try {
            requestService.deleteScheduleRequest(requestId);
            return ApiResponse.success(ResponseCode.OK, "일정 추가 요청 삭제 완료");
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
