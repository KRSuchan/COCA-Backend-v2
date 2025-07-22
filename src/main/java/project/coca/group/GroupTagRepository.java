package project.coca.group;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.tag.GroupTag;

public interface GroupTagRepository extends JpaRepository<GroupTag, Long> {
    void deleteAllByCoGroupId(Long groupId);
}
