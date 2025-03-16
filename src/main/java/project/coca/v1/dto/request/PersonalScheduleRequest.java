package project.coca.v1.dto.request;

import lombok.Getter;
import lombok.Setter;
import project.coca.domain.personal.Member;
import project.coca.domain.personal.PersonalSchedule;

@Getter
@Setter
public class PersonalScheduleRequest {
    private PersonalSchedule personalSchedule;
    private Member member;
}
