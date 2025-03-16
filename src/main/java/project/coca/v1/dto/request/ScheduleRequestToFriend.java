package project.coca.v1.dto.request;

import lombok.Getter;
import lombok.Setter;
import project.coca.domain.personal.Friend;
import project.coca.domain.personal.Member;
import project.coca.domain.request.RequestedSchedule;

import java.util.List;

@Setter
@Getter
public class ScheduleRequestToFriend {
    private Member member;
    private RequestedSchedule requestedSchedule;
    private List<Friend> friends;
}
