package project.coca.schedule.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupScheduleAttachmentRepository extends JpaRepository<GroupScheduleAttachment, Long> {
    @Query("select ga from GroupScheduleAttachment ga where ga.groupSchedule.id = :scheduleId")
    List<GroupScheduleAttachment> findGroupScheduleAttachmentByGroupScheduleId(Long scheduleId);
}
