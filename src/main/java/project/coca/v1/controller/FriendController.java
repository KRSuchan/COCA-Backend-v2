package project.coca.v1.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import project.coca.domain.personal.Friend;
import project.coca.domain.personal.PersonalSchedule;
import project.coca.v1.dto.response.common.ApiResponse;
import project.coca.v1.dto.response.common.error.ErrorCode;
import project.coca.v1.dto.response.common.success.ResponseCode;
import project.coca.v1.dto.response.friend.FriendResponse;
import project.coca.v1.dto.response.friend.FriendScheduleResponse;
import project.coca.v1.service.FriendService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/friend")
public class FriendController {
    private final FriendService friendService;

    /**
     * 32. 친구 일정 조회 (공개 일정은 표시, 비공개 일정은 "비공개 일정"으로 제목 변환)
     *
     * @param friendId 친구 관계 id 필요
     * @return : 일정 start, end, 일정 제목
     */
    @GetMapping("/schedule/friendId/{friendId}")
    public ApiResponse<List<FriendScheduleResponse>> getFriendSchedule(@PathVariable Long friendId) {
        log.info("find Friend Schedule friendId {}", friendId);
        try {
            List<PersonalSchedule> schedules = friendService.findFriendSchedule(friendId);
            List<FriendScheduleResponse> data = schedules
                    .stream()
                    .map(FriendScheduleResponse::of)
                    .collect(Collectors.toList());
            return ApiResponse.response(ResponseCode.OK, data);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 33. 친구 목록 조회
     *
     * @param memberId 회원 id(String) 필요
     * @return : 친구 id(Long), 친구 이름, 친구 프로필 사진 경로
     */
    @GetMapping("/list/memberId/{memberId}")
    public ApiResponse<List<FriendResponse>> getFriendList(@PathVariable String memberId) {
        log.info("get friend list memberId {}", memberId);
        try {
            List<Friend> friends = friendService.findFriends(memberId);
            List<FriendResponse> data = friends
                    .stream()
                    .map(FriendResponse::of)
                    .collect(Collectors.toList());
            return ApiResponse.response(ResponseCode.OK, data);
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 34. 친구 수정(닉네임 수정)
     *
     * @param updateFriend 친구 관계 id 필요
     */
    @PutMapping("/update")
    public ApiResponse<?> updateFriend(@RequestBody Friend updateFriend) {
        log.info("update friend {}", updateFriend);
        try {
            friendService.updateFriend(updateFriend);
            return ApiResponse.success(ResponseCode.OK, "친구 수정 완료");
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 35. 친구 삭제
     * 나의 친구관계, 친구의 친구관계 두개 삭제함.
     *
     * @param friendId 친구 관계 id 필요
     */
    @DeleteMapping("/delete/{friendId}")
    public ApiResponse<?> deleteFriend(@PathVariable Long friendId) {
        log.info("delete friend {}", friendId);
        try {
            friendService.deleteFriend(friendId);
            return ApiResponse.success(ResponseCode.OK, "친구 삭제 완료");
        } catch (NoSuchElementException e) {
            return ApiResponse.fail(ErrorCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
