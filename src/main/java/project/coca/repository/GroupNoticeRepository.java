package project.coca.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.domain.group.GroupNotice;

public interface GroupNoticeRepository extends JpaRepository<GroupNotice, Long> {

    void deleteByCoGroupId(Long groupId);

    GroupNotice findByCoGroupId(Long id);
}
