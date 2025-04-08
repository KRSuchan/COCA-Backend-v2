package project.coca.request;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.domain.request.RequestedSchedule;

public interface RequestedScheduleRepository extends JpaRepository<RequestedSchedule, Long> {
}
