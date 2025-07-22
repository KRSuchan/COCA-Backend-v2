package project.coca.request;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.member.Member;

import java.util.List;

public interface ScheduleRequestRepository extends JpaRepository<ScheduleRequest, Long> {
    List<ScheduleRequest> findByReceiverOrderByCreatedDateDesc(Member member);
}
