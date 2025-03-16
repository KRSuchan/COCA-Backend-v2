package project.coca.domain.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
public class GroupScheduleAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_SCHEDULE_ATTACHMENT_ID")
    private Long id;
    @Column(name = "FILE_NAME")
    private String fileName;
    @Column(name = "FILE_PATH")
    private String filePath;
    @Column(name = "FILE_HASH")
    private String fileMd5;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GROUP_SCHEDULE_ID", nullable = false)
    private GroupSchedule groupSchedule;
}
