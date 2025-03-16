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
public class GroupScheduleHeart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_SCHEDULE_HEART_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_MEMBER_ID", nullable = false)
    private GroupMember groupMember;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "GROUP_SCHEDULE_ID", nullable = false)
    private GroupSchedule groupSchedule;
}
