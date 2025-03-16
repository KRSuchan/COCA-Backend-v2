package project.coca.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.domain.personal.Member;
import project.coca.domain.request.ScheduleRequest;

import java.util.List;

public interface ScheduleRequestRepository extends JpaRepository<ScheduleRequest, Long> {
    List<ScheduleRequest> findByReceiverOrderByCreatedDateDesc(Member member);
}
