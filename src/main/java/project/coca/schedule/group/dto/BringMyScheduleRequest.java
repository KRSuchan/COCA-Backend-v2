package project.coca.schedule.group.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BringMyScheduleRequest {
    private Long groupId;
    private LocalDate date;
}
