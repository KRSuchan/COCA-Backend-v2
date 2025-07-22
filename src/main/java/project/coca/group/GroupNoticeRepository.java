package project.coca.group;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupNoticeRepository extends JpaRepository<GroupNotice, Long> {

    void deleteByCoGroupId(Long groupId);

    GroupNotice findByCoGroupId(Long id);
}
