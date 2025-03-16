package project.coca.v1.dto.response.GroupSchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.coca.domain.group.GroupSchedule;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class GroupScheduleSummaryResponse {
    private Long id;
    private String title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String color;

    public static GroupScheduleSummaryResponse of(GroupSchedule groupSchedule) {
        return GroupScheduleSummaryResponse.builder()
                .id(groupSchedule.getId())
                .title(groupSchedule.getTitle())
                .startTime(groupSchedule.getStartTime())
                .endTime(groupSchedule.getEndTime())
                .color(groupSchedule.getColor())
                .build();
    }
}
