package project.coca.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.coca.aop.ExeTimer;
import project.coca.domain.personal.Member;
import project.coca.domain.personal.PersonalSchedule;
import project.coca.domain.personal.PersonalScheduleAttachment;
import project.coca.member.MemberRepository;
import project.coca.schedule.request.PersonalScheduleRequest;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PersonalScheduleService {
    private final PersonalScheduleRepository personalScheduleRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final PersonalScheduleAttachmentRepository personalScheduleAttachmentRepository;

    /**
     * 09. 개인 일정 등록
     *
     * @param request : 작성한 개인 일정 요청 데이터
     * @return : 저장된 개인 일정 return
     * timer : 첨부파일이 없을때 9~11ms /
     */
    @ExeTimer
    @Transactional
    public PersonalSchedule savePersonalSchedule(String username,
                                                 PersonalScheduleRequest request,
                                                 MultipartFile[] attachments) throws IOException {
        Member foundMember = memberRepository.findById(username)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        // DTO를 Entity로 변환
        PersonalSchedule personalSchedule = request.toEntity();
        personalSchedule.setMember(foundMember);

        // 일정 저장
        PersonalSchedule savedSchedule = personalScheduleRepository.save(personalSchedule);

        // 새로운 첨부 파일 추가
        if (attachments != null) { // 첨부파일이 존재 확인
            for (MultipartFile attachment : attachments) {
                if (attachment != null) {
                    saveAttachment(username, savedSchedule, attachment);
                }
            }
        }
        return savedSchedule;
    }

    /**
     * 10. 개인 일정 목록 조회 (요약 정보)
     * 11. 개인 일정 상세 정보 조회 (목록으로 반환)
     *
     * @param memberId 회원 계정 id
     * @param start    기간 시작
     * @param end      기간 끝
     * @return List<PersonalSchedule>
     */
    @ExeTimer
    public List<PersonalSchedule> findPersonalSchedulesByDates(String memberId, LocalDate start, LocalDate end) {
        // LocalDate 에서 LocalDateTime 변환
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.of(23, 59, 59));
        // 기간 일정 목록 조회
        return personalScheduleRepository.findPersonalScheduleByDateRange(memberId, startDT, endDT);
    }


    /**
     * 12. 개인 일정 수정
     */
    @ExeTimer
    @Transactional
    public PersonalSchedule updatePersonalSchedule(String username,
                                                   PersonalScheduleRequest request,
                                                   MultipartFile[] attachments) throws IOException {
        PersonalSchedule found = personalScheduleRepository.findById(request.getId())
                .orElseThrow(() -> new NoSuchElementException("일정이 조회되지 않습니다."));

        // 수정된 내용 반영
        found.update(
                request.getTitle(),
                request.getDescription(),
                request.getLocation(),
                request.getStartTime(),
                request.getEndTime(),
                request.getColor(),
                request.getIsPrivate()
        );

        // 기존 첨부 파일 삭제
        List<PersonalScheduleAttachment> oldAttachments = found.getAttachments();
        for (PersonalScheduleAttachment old : oldAttachments) {
            s3Service.deleteS3File(old.getFilePath());
        }
        personalScheduleAttachmentRepository.deleteAllByPersonalSchedule(found);
        found.getAttachments().clear();

        // 새로운 첨부 파일 추가
        if (attachments != null) { // null 체크 추가
            for (MultipartFile attachment : attachments) {
                if (attachment != null) { // 논리 AND 조건으로 수정
                    saveAttachment(username, found, attachment);
                }
            }
        }

        return found;
    }


    private void saveAttachment(String username, PersonalSchedule personalSchedule, MultipartFile attachment) throws IOException {
        URL savedUrl = s3Service.uploadPersonalScheduleFile(attachment, username, personalSchedule.getId(), 0);
        PersonalScheduleAttachment personalScheduleAttachment = PersonalScheduleAttachment.builder()
                .fileName(attachment.getOriginalFilename())
                .filePath(savedUrl.toString())
                .personalSchedule(personalSchedule)
                .build();
        personalSchedule.getAttachments().add(personalScheduleAttachment);
        log.info("총 저장된 첨부파일 {}", personalSchedule.getAttachments().size());
    }

    /**
     * 13. 개인 일정 삭제
     */
    @ExeTimer
    @Transactional
    public void deletePersonalScheduleById(String memberId, Long personalScheduleId) {
        Member foundMember = memberRepository.findById(memberId).orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않았습니다."));

        PersonalSchedule foundPersonalSchedule = personalScheduleRepository.findById(personalScheduleId)
                .orElseThrow(() -> new NoSuchElementException("일정이 조회되지 않았습니다."));

        List<PersonalScheduleAttachment> files = personalScheduleAttachmentRepository.findByPersonalSchedule(foundPersonalSchedule);
        // 각 파일의 URL을 사용하여 S3에서 파일 삭제
        for (PersonalScheduleAttachment file : files) {
            String path = file.getFilePath();
            System.out.println(path);
            s3Service.deleteS3File(path); // S3에서 파일 삭제
        }
        // 일정 삭제 수행
        personalScheduleRepository.deleteById(personalScheduleId);
    }

}
