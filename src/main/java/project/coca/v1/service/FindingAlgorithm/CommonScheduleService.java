package project.coca.v1.service.FindingAlgorithm;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.coca.domain.personal.Member;
import project.coca.domain.personal.PersonalSchedule;
import project.coca.v1.dto.request.FindEmptyScheduleRequest;
import project.coca.v1.dto.response.CommonSchedule;
import project.coca.v1.dto.response.personalSchedule.PersonalScheduleForEmptyScheduleResponse;
import project.coca.v1.repository.MemberRepository;
import project.coca.v1.repository.PersonalScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommonScheduleService {
    @Autowired
    private final PersonalScheduleRepository personalScheduleRepository;
    @Autowired
    private final MemberRepository memberRepository;

    //하루는 무조건 00~00으로 침
    //날짜 단위 -> DP 활용 브루트포스
    //시간 단위 -> 인터벌 알고리즘
    //날짜 + 시간 -> 인터벌 알고리즘
    //n => 분

    public List<PersonalScheduleForEmptyScheduleResponse> memberScheduleReq(FindEmptyScheduleRequest memberList) {
        LocalDateTime startTime = memberList.getStartDate().atStartOfDay();
        LocalDateTime endTime = memberList.getEndDate().atTime(23, 59, 59);

        List<PersonalScheduleForEmptyScheduleResponse> result = new ArrayList<>();

        if (memberList.getMembers() != null && memberList.getMembers().size() > 0) {
            for (String memberId : memberList.getMembers()) {
                Member member = memberRepository.findById(memberId).orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
                List<CommonSchedule> schedule = personalScheduleRepository.findPersonalScheduleByDateRange(memberId, startTime, endTime)
                        .stream().map(CommonSchedule::of).collect(Collectors.toList());

                result.add(new PersonalScheduleForEmptyScheduleResponse(member.getId(), member.getUserName(), schedule));
            }
        }
        return result;
    }

    public List<CommonSchedule> findEmptySchedule(FindEmptyScheduleRequest request) {
        if (request.getFindDay() > 0) {
            int period = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1; //if(24~29)면 6일 필요

            return bruteForce(request.getStartDate(), request.getFindDay(), period, request.getMembers());
        } else if (request.getFindMinute() > 0) {
            LocalDateTime startTime = request.getStartDate().atStartOfDay();
            LocalDateTime endTime = request.getEndDate().atTime(23, 59, 59);

            return interval(startTime, endTime, request.getFindMinute(), request.getMembers());
        } else {
            throw new NoSuchElementException("찾을 일정의 시간/날을 입력하지 않았습니다.");
        }
    }

    private List<CommonSchedule> bruteForce(LocalDate startDate, int duration, int period, List<String> members) {
        boolean daySlot[] = new boolean[period];
        Arrays.fill(daySlot, true);

        for (String memberId : members) {
            LocalDateTime startTime = startDate.atTime(0, 0, 1);
            LocalDateTime endTime = startDate.atTime(23, 59, 59);

            for (int i = 0; i < period; i++) {
                //이미 그날에 일정이 있는거면(daySlot[i] == false) 굳이 확인할 필요 없음. 이미 땡인 날임
                if (daySlot[i]) {
                    List<PersonalSchedule> personalSchedules = personalScheduleRepository.findPersonalScheduleByDateRange(memberId, startTime, endTime);

                    if (personalSchedules != null && personalSchedules.size() > 0)
                        daySlot[i] = false;
                }
                startTime = startTime.plusDays(1);
                endTime = endTime.plusDays(1);
            }
        }

        List<CommonSchedule> resultSchedule = new ArrayList<>();

        for (int i = 0; i <= daySlot.length - duration; i++) {
            boolean isAvailable = true;
            for (int j = 0; j < duration; j++) {
                if (!daySlot[i + j]) {
                    isAvailable = false;
                    break;
                }
            }
            if (isAvailable)
                resultSchedule.add(new CommonSchedule(startDate.atStartOfDay().plusDays(i), startDate.atStartOfDay().plusDays(duration + i)));
        }
        return resultSchedule;
    }

    private List<CommonSchedule> interval(LocalDateTime startTime, LocalDateTime endTime, int duration, List<String> members) {
        List<Interval> combined = new ArrayList<>();
        final int tineSlot = 10;

        for (String memberId : members) {
            List<PersonalSchedule> personalSchedules = personalScheduleRepository.findPersonalScheduleByDateRange(memberId, startTime, endTime);

            if (personalSchedules != null && personalSchedules.size() > 0) {
                for (PersonalSchedule schedule : personalSchedules)
                    combined.add(new Interval(schedule.getStartTime(), schedule.getEndTime()));
            }
        }
        List<Interval> mergeSchedule = IntervalMerge.intervalMerge(combined);
        Collections.sort(mergeSchedule, Comparator.comparing(Interval::getStart));

        List<CommonSchedule> resultSchedule = new ArrayList<>(); //빈 일정이 담기는 리스트
        LocalDateTime current = startTime;

        for (Interval interval : mergeSchedule) {
            while ((current.plusMinutes(duration).isBefore(interval.getStart()) || current.plusMinutes(duration).isEqual(interval.getStart()))) {
                resultSchedule.add(new CommonSchedule(current, current.plusMinutes(duration)));
                current = current.plusMinutes(tineSlot); // 다음 빈 시간대를 검색하기 위해 시간 증가
            }
            // 현재 시간을 다음 일정의 끝 시간으로 업데이트
            if (current.isBefore(interval.getEnd()))
                current = interval.getEnd();
        }

        while (current.plusMinutes(duration).isBefore(endTime) || current.plusMinutes(duration).isEqual(endTime)) {
            resultSchedule.add(new CommonSchedule(current, current.plusMinutes(duration)));
            current = current.plusMinutes(tineSlot);
        }
        return resultSchedule;
    }
}
