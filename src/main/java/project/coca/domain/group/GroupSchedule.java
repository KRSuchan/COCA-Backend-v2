package project.coca.domain.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
public class GroupSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_SCHEDULE_ID")
    private Long id;

    @Column(name = "TITLE", length = 45, nullable = false)
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

    @Column(name = "COLOR", length = 7, nullable = false)
    private String color;

    // 그룹 일정 가져오기, (하트)
    @OneToMany(mappedBy = "groupSchedule", cascade = CascadeType.ALL)
    private List<GroupScheduleHeart> hearts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID", nullable = false)
    private CoGroup coGroup;

    @OneToMany(mappedBy = "groupSchedule", cascade = CascadeType.ALL)
    private List<GroupScheduleAttachment> groupScheduleAttachments = new ArrayList<>();

    public void addAttachment(GroupScheduleAttachment newAttach) {
        groupScheduleAttachments.add(newAttach);
    }

    public void removeAttachment(GroupScheduleAttachment deleteAttachment) {
        groupScheduleAttachments.remove(deleteAttachment);
    }
}
