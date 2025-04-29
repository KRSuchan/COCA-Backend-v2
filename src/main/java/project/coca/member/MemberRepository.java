package project.coca.member;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.domain.personal.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
}
