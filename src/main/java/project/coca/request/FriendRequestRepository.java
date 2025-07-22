package project.coca.request;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.member.Member;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    List<FriendRequest> findByReceiverOrderByCreatedDateDesc(Member receiver);

    Optional<FriendRequest> findBySenderAndReceiverAndRequestStatus(Member member, Member opponent, RequestStatus status);
}
