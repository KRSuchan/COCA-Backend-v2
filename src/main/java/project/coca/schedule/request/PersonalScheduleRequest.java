package project.coca.schedule.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import project.coca.domain.personal.PersonalSchedule;

import java.time.LocalDateTime;

@Getter
@Setter
public class PersonalScheduleRequest {
    private Long id; // 수정 시 필요

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 45, message = "제목은 45자 이하여야 합니다")
    private String title;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    private String description;

    @Size(max = 50, message = "위치는 50자 이하여야 합니다")
    private String location;

    @NotNull(message = "시작 시간은 필수입니다")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간은 필수입니다")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotBlank(message = "색상은 필수입니다")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 #RRGGBB 형식이어야 합니다")
    private String color;

    @NotNull(message = "공개 여부는 필수입니다")
    private Boolean isPrivate;

    public PersonalSchedule toEntity() {
        return PersonalSchedule.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .location(this.location)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .color(this.color)
                .isPrivate(this.isPrivate)
                .build();
    }

    @AssertTrue(message = "종료 시간은 시작 시간보다 늦어야 합니다")
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return true; // null 체크는 @NotNull에서 처리
        }
        return endTime.isAfter(startTime);
    }
}
