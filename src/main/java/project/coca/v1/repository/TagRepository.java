package project.coca.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.domain.tag.Tag;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
}
