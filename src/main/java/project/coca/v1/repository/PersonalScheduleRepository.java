package project.coca.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.coca.domain.personal.PersonalSchedule;

import java.time.LocalDateTime;
import java.util.List;

public interface PersonalScheduleRepository extends JpaRepository<PersonalSchedule, Long> {
    /**
     * 개인 일정 목록 조회
     *
     * @param memberId  회원 id
     * @param startDate 일정 시작
     * @param endDate   일정 끝
     * @return 회원 일정 목록
     * <p>
     * Query
     * 1. memberId가 동일한가
     * 2. startTime 혹은 endTime이 startDate와 endDate 사이에 있는가
     * 3. startTime이 startDate 이전이고 endTime이 startDate 이후인가
     */
    @Query("SELECT s FROM PersonalSchedule s WHERE s.member.id = :memberId AND " +
            "(((s.startTime BETWEEN :startDate AND :endDate) OR (s.endTime BETWEEN :startDate AND :endDate)) OR " +
            "((s.startTime < :startDate) AND (s.endTime > :endDate)))" +
            "ORDER BY s.startTime ASC")
    List<PersonalSchedule> findPersonalScheduleByDateRange(String memberId, LocalDateTime startDate, LocalDateTime endDate);
}
