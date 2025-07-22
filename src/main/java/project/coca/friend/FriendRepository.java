package project.coca.friend;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.member.Member;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findAllByMember(Member member);

    Optional<Friend> findByMemberAndOpponent(Member member, Member opponent);
}
