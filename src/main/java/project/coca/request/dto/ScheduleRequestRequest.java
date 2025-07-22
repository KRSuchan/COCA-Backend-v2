package project.coca.request.dto;

import lombok.Getter;
import lombok.Setter;
import project.coca.member.Member;
import project.coca.request.RequestedSchedule;

import java.util.List;

@Setter
@Getter
public class ScheduleRequestRequest {
    private Member sender;
    private RequestedSchedule requestedSchedule;
    private List<Member> receivers;
}
