package project.coca.member;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.domain.tag.Interest;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    void deleteById(Long id);
}
