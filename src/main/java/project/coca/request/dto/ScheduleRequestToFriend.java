package project.coca.request.dto;

import lombok.Getter;
import lombok.Setter;
import project.coca.friend.Friend;
import project.coca.member.Member;
import project.coca.request.RequestedSchedule;

import java.util.List;

@Setter
@Getter
public class ScheduleRequestToFriend {
    private Member member;
    private RequestedSchedule requestedSchedule;
    private List<Friend> friends;
}
