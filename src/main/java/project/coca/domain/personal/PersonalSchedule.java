package project.coca.domain.personal;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PersonalSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERSONAL_SCHEDULE_ID")
    private Long id;

    @Column(name = "TITLE", nullable = false, length = 45)
    private String title;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "LOCATION", length = 50)
    private String location;

    @Column(name = "START_TIME", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Column(name = "END_TIME", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Column(name = "COLOR", nullable = false, length = 7)
    private String color;

    @Column(name = "IS_PRIVATE", nullable = false)
    private Boolean isPrivate;

    @Builder.Default
    @OneToMany(mappedBy = "personalSchedule", cascade = CascadeType.ALL)
    private List<PersonalScheduleAttachment> attachments = new ArrayList<>();

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    public void update(String title, String description, String location,
                       LocalDateTime startTime, LocalDateTime endTime,
                       String color, Boolean isPrivate) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.color = color;
        this.isPrivate = isPrivate;
    }

    public void update(List<PersonalScheduleAttachment> attachments) {
        this.attachments = attachments;
    }
}
