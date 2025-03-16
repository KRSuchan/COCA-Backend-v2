package project.coca.v1.dto.response.GroupSchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.coca.domain.group.GroupSchedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
public class GroupScheduleResponse {
    private Long groupId;
    private Long scheduleId;
    private String title;
    private String description;
    private String location;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String color;
    private int hearts; //하트 개수
    private List<GroupScheduleAttachmentResponse> attachments;

    public static GroupScheduleResponse of(GroupSchedule schedule) {
        return GroupScheduleResponse.builder()
                .groupId(schedule.getCoGroup().getId())
                .scheduleId(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .location(schedule.getLocation())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .color(schedule.getColor())
                .hearts(schedule.getHearts().size())
                .attachments(schedule.getGroupScheduleAttachments()
                        .stream().map(GroupScheduleAttachmentResponse::of)
                        .collect(Collectors.toList()))
                .build();
    }
}
