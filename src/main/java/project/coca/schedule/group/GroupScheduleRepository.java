package project.coca.schedule.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupScheduleRepository extends JpaRepository<GroupSchedule, Long> {
    void delete(GroupSchedule groupSchedule);

    Optional<GroupSchedule> findById(Long id);

    boolean existsById(Long id);

    @Query("select s from GroupSchedule s where s.coGroup.id = :groupId and ((s.startTime <= :startDate and s.endTime >= :endDate) " +
            "or ((s.startTime between :startDate and :endDate) or (s.endTime between :startDate and :endDate))) order by s.startTime asc")
    List<GroupSchedule> findGroupSchedule(Long groupId, LocalDateTime startDate, LocalDateTime endDate);

}
