package project.coca.schedule.personal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonalScheduleAttachmentRepository extends JpaRepository<PersonalScheduleAttachment, Long> {
    void deleteAllByPersonalSchedule(PersonalSchedule personalSchedule);

    List<PersonalScheduleAttachment> findByPersonalSchedule(PersonalSchedule personalSchedule);
}
