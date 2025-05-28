package project.coca.schedule.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BringMyScheduleRequest {
    private Long groupId;
    private String memberId;
    private LocalDate date;
}
