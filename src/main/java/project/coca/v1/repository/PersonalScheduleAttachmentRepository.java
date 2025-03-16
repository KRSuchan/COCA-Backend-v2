package project.coca.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.coca.domain.personal.PersonalSchedule;
import project.coca.domain.personal.PersonalScheduleAttachment;

import java.util.List;

public interface PersonalScheduleAttachmentRepository extends JpaRepository<PersonalScheduleAttachment, Long> {
    void deleteAllByPersonalSchedule(PersonalSchedule personalSchedule);

    List<PersonalScheduleAttachment> findByPersonalSchedule(PersonalSchedule personalSchedule);
}
