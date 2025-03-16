package project.coca.v1.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import project.coca.domain.personal.PersonalSchedule;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonSchedule {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    public static CommonSchedule of(PersonalSchedule personalSchedule) {
        return CommonSchedule.builder()
                .startTime(personalSchedule.getStartTime())
                .endTime(personalSchedule.getEndTime())
                .build();
    }
}
