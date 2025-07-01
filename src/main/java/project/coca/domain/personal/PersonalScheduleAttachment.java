package project.coca.domain.personal;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@ToString
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PersonalScheduleAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERSONAL_SCHEDULE_ATTACHMENT_ID")
    private Long id;
    @Column(name = "FILE_NAME", nullable = false)
    private String fileName;
    @Column(name = "FILE_PATH", nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PERSONAL_SCHEDULE_ID")
    private PersonalSchedule personalSchedule;

    public void update(String filename, String filePath, PersonalSchedule personalSchedule) {
        this.fileName = filename;
        this.filePath = filePath;
        this.personalSchedule = personalSchedule;
    }
}
