package project.coca.v1.dto.response.personalSchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import project.coca.domain.personal.PersonalSchedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PersonalScheduleResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String color;
    private Boolean isPrivate;
    private List<PersonalScheduleAttachmentResponse> attachments;

    public static PersonalScheduleResponse of(PersonalSchedule personalSchedule) {
        return PersonalScheduleResponse.builder()
                .id(personalSchedule.getId())
                .title(personalSchedule.getTitle())
                .description(personalSchedule.getDescription())
                .location(personalSchedule.getLocation())
                .startTime(personalSchedule.getStartTime())
                .endTime(personalSchedule.getEndTime())
                .color(personalSchedule.getColor())
                .isPrivate(personalSchedule.getIsPrivate())
                .attachments(personalSchedule.getAttachments().stream()
                        .map(PersonalScheduleAttachmentResponse::of)
                        .collect(Collectors.toList()))
                .build();
    }
}
