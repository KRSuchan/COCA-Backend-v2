package project.coca.schedule.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalScheduleForEmptyScheduleResponse {
    String memberId;
    String memberName;
    List<CommonSchedule> scheduleList = new ArrayList<>();
}
