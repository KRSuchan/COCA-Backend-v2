package project.coca.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.coca.domain.group.GroupScheduleAttachment;

import java.util.List;

public interface GroupScheduleAttachmentRepository extends JpaRepository<GroupScheduleAttachment, Long> {
    GroupScheduleAttachment save(GroupScheduleAttachment groupScheduleAttachment);

    void delete(GroupScheduleAttachment groupScheduleAttachment);

    @Query("select ga from GroupScheduleAttachment ga where ga.groupSchedule.id = :scheduleId")
    List<GroupScheduleAttachment> findGroupScheduleAttachmentByGroupScheduleId(Long scheduleId);
}
