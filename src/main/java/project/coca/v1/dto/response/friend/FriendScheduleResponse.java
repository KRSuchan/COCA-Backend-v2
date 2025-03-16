package project.coca.v1.dto.response.friend;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.personal.PersonalSchedule;

import java.time.LocalDateTime;

@Data
@Builder
public class FriendScheduleResponse {
    private String title;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Boolean isPrivate;

    public static FriendScheduleResponse of(PersonalSchedule personalSchedule) {
        return FriendScheduleResponse.builder()
                .isPrivate(personalSchedule.getIsPrivate())
                .startDateTime(personalSchedule.getStartTime())
                .endDateTime(personalSchedule.getEndTime())
                .title(personalSchedule.getIsPrivate() ? "비공개 일정" : personalSchedule.getTitle())
                .build();
    }
}
