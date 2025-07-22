package project.coca.request;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.group.CoGroup;
import project.coca.member.Member;

import java.util.List;
import java.util.Optional;

public interface GroupRequestRepository extends JpaRepository<GroupRequest, Long> {
    List<GroupRequest> findByReceiverOrderByCreatedDateDesc(Member receiver);

    Optional<GroupRequest> findByCoGroupAndReceiverAndRequestStatus(CoGroup group, Member receiver, RequestStatus requestStatus);
}
