package project.coca.domain.request;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import project.coca.domain.personal.Member;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)  // 감사 기능 활성화
public class ScheduleRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SCHEDULE_REQUEST_ID")
    private Long id;
    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;
    @CreatedDate
    @Column(updatable = false, name = "CREATED_AT")
    private LocalDateTime createdDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SENDER_ID", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECEIVER_ID", nullable = false)
    private Member receiver;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "REQUESTED_SCHEDULE_ID", nullable = false)
    private RequestedSchedule requestedSchedule;
}
