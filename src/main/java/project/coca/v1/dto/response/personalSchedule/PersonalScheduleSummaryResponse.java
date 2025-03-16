package project.coca.v1.dto.response.personalSchedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import project.coca.domain.personal.PersonalSchedule;

import java.time.LocalDateTime;

@Data
@Builder
public class PersonalScheduleSummaryResponse {
    private Long id;
    private String title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String color;
    private Boolean isPrivate;

    public static PersonalScheduleSummaryResponse of(PersonalSchedule personalSchedule) {
        return PersonalScheduleSummaryResponse.builder()
                .id(personalSchedule.getId())
                .title(personalSchedule.getTitle())
                .startTime(personalSchedule.getStartTime())
                .endTime(personalSchedule.getEndTime())
                .color(personalSchedule.getColor())
                .isPrivate(personalSchedule.getIsPrivate())
                .build();
    }
}
