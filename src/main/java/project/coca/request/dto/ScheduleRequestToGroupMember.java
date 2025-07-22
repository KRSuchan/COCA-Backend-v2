package project.coca.request.dto;

import lombok.Getter;
import lombok.Setter;
import project.coca.member.Member;
import project.coca.request.RequestedSchedule;

import java.util.List;

@Setter
@Getter
public class ScheduleRequestToGroupMember {
    private Long groupId;
    private Member manager;
    private RequestedSchedule requestedSchedule;
    private List<Member> groupMembers;
}
