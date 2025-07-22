package project.coca.schedule.personal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.coca.aop.ExeTimer;
import project.coca.base.exception.MemberNotFoundException;
import project.coca.base.exception.ScheduleNotFoundException;
import project.coca.member.Member;
import project.coca.member.MemberRepository;
import project.coca.schedule.S3Service;
import project.coca.schedule.personal.dto.PersonalScheduleRequest;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
     * 개인 일정 등록
     *
     * @param username    일정을 입력할 사용자의 아이디
     * @param request     작성한 개인 일정 요청 데이터
     * @param attachments 작성한 개인 일정의 첨부파일 nullable
     * @return : added PersonalSchedule
     */
    @ExeTimer
    @Transactional
    public PersonalSchedule add(String username,
                                PersonalScheduleRequest request,
                                MultipartFile[] attachments) {
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
     * 개인 일정 정보 조회
     *
     * @param username  회원 계정 id
     * @param startDate 기간 시작
     * @param endDate   기간 끝
     * @return List<PersonalSchedule>
     */
    @ExeTimer
    public List<PersonalSchedule> findByDates(String username, LocalDate startDate, LocalDate endDate) {
        // LocalDate 에서 LocalDateTime 변환
        LocalDateTime startDT = startDate.atStartOfDay();
        LocalDateTime endDT = endDate.atTime(LocalTime.of(23, 59, 59));
        // 기간 일정 목록 조회
        return personalScheduleRepository.findPersonalScheduleByDateRange(username, startDT, endDT);
    }


    /**
     * 개인 일정 수정
     *
     * @param username    일정을 입력할 사용자의 아이디
     * @param request     작성한 개인 일정 요청 데이터
     * @param attachments 작성한 개인 일정에 등록할 첨부파일 nullable
     * @return : updated PersonalSchedule
     */
    @ExeTimer
    @Transactional
    public PersonalSchedule update(String username,
                                   PersonalScheduleRequest request,
                                   MultipartFile[] attachments) {
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


    private void saveAttachment(String username, PersonalSchedule personalSchedule, MultipartFile attachment) {
        URL savedUrl = s3Service.uploadPersonalScheduleFile(attachment, username, personalSchedule.getId(), 0);
        PersonalScheduleAttachment personalScheduleAttachment = PersonalScheduleAttachment.builder()
                .fileName(attachment.getOriginalFilename())
                .filePath(savedUrl.toString())
                .personalSchedule(personalSchedule)
                .build();
        personalSchedule.getAttachments().add(personalScheduleAttachment);
        log.info("총 저장된 첨부파일 {}", Optional.of(personalSchedule.getAttachments().size()));
    }

    /**
     * 개인 일정 삭제
     *
     * @param username           일정을 입력할 사용자의 아이디
     * @param personalScheduleId 작성한 개인 일정 요청 데이터
     */
    @ExeTimer
    @Transactional
    public void deleteById(String username, Long personalScheduleId) {
        Member foundMember = memberRepository.findById(username).orElseThrow(() -> new MemberNotFoundException("회원이 조회되지 않았습니다."));

        PersonalSchedule foundPersonalSchedule = personalScheduleRepository.findById(personalScheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException("일정이 조회되지 않았습니다."));

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
