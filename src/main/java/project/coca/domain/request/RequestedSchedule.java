package project.coca.domain.request;

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
public class RequestedSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REQUESTED_SCHEDULE_ID")
    private Long id;
    @Column(name = "TITLE", length = 45, nullable = false)
    private String title;
    @Column(name = "DESCRIPTION", length = 500)
    private String description;
    @Column(name = "LOCATION", length = 50)
    private String location;
    @Column(name = "START_TIME", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @Column(name = "END_TIME", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    @Column(name = "COLOR", length = 7, nullable = false)
    private String color;

    @OneToMany(mappedBy = "requestedSchedule", cascade = CascadeType.ALL)
    private List<ScheduleRequest> scheduleRequests = new ArrayList<>();
}
