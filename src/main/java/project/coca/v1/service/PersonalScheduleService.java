package project.coca.v1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.coca.domain.personal.Member;
import project.coca.domain.personal.PersonalSchedule;
import project.coca.domain.personal.PersonalScheduleAttachment;
import project.coca.v1.repository.MemberRepository;
import project.coca.v1.repository.PersonalScheduleAttachmentRepository;
import project.coca.v1.repository.PersonalScheduleRepository;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class PersonalScheduleService {
    private final PersonalScheduleRepository personalScheduleRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final PersonalScheduleAttachmentRepository personalScheduleAttachmentRepository;

    @Autowired
    public PersonalScheduleService(PersonalScheduleRepository personalScheduleRepository,
                                   MemberRepository memberRepository,
                                   S3Service s3Service, PersonalScheduleAttachmentRepository personalScheduleAttachmentRepository) {
        this.personalScheduleRepository = personalScheduleRepository;
        this.memberRepository = memberRepository;
        this.s3Service = s3Service;
        this.personalScheduleAttachmentRepository = personalScheduleAttachmentRepository;
    }

    /**
     * 09. 개인 일정 등록
     *
     * @param personalSchedule : 작성한 개인 일정 id를 제외한 전체
     * @return : 저장된 개인 일정 return
     */
    public PersonalSchedule savePersonalSchedule(Member member,
                                                 PersonalSchedule personalSchedule,
                                                 MultipartFile[] attachments) throws IOException {
        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        // 일정에 회원 반영
        personalSchedule.setMember(foundMember);

        PersonalSchedule savedSchedule = personalScheduleRepository.save(personalSchedule);

        // 새로운 첨부 파일 추가
        if (attachments != null && attachments.length > 0) { // null 체크 추가
            for (MultipartFile attachment : attachments) {
                if (attachment != null) { // 논리 AND 조건으로 수정
                    saveAttachment(member, savedSchedule, attachment);
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
    public List<PersonalSchedule> findPersonalSchedulesByDates(String memberId, LocalDate start, LocalDate end) {
        Member member =
                memberRepository.findById(memberId).orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        // LocalDate 에서 LocalDateTime 변환
        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(LocalTime.of(23, 59, 59));
        // 기간 일정 목록 조회
        return personalScheduleRepository.findPersonalScheduleByDateRange(memberId, startDT, endDT);
    }


    /**
     * 12. 개인 일정 수정
     */
    public PersonalSchedule updatePersonalSchedule(Member member,
                                                   PersonalSchedule updatePersonalSchedule,
                                                   MultipartFile[] attachments) throws IOException {
        Member foundMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        PersonalSchedule foundPersonalSchedule = personalScheduleRepository.findById(updatePersonalSchedule.getId())
                .orElseThrow(() -> new NoSuchElementException("일정이 조회되지 않습니다."));
        System.out.println("회원 조회, 일정 조회 완료");

        // 수정된 내용 반영
        foundPersonalSchedule.setTitle(updatePersonalSchedule.getTitle());
        foundPersonalSchedule.setDescription(updatePersonalSchedule.getDescription());
        foundPersonalSchedule.setLocation(updatePersonalSchedule.getLocation());
        foundPersonalSchedule.setStartTime(updatePersonalSchedule.getStartTime());
        foundPersonalSchedule.setEndTime(updatePersonalSchedule.getEndTime());
        foundPersonalSchedule.setColor(updatePersonalSchedule.getColor());
        foundPersonalSchedule.setIsPrivate(updatePersonalSchedule.getIsPrivate());

        // 기존 첨부 파일 삭제
        personalScheduleAttachmentRepository.deleteAllByPersonalSchedule(foundPersonalSchedule);
        personalScheduleAttachmentRepository.flush();

        System.out.println("수정된 내용 반영 완료");
        System.out.println(foundPersonalSchedule.getAttachments().size());
        System.out.println(updatePersonalSchedule.getAttachments().size());
        foundPersonalSchedule.setAttachments(updatePersonalSchedule.getAttachments());
        System.out.println("받아온 첨부파일 " + foundPersonalSchedule.getAttachments().size());

        // 새로운 첨부 파일 추가
        if (attachments != null && attachments.length > 0) { // null 체크 추가
            for (MultipartFile attachment : attachments) {
                if (attachment != null) { // 논리 AND 조건으로 수정
                    saveAttachment(member, foundPersonalSchedule, attachment);
                }
            }
        }

        System.out.println("첨부파일 반영 완료");
        System.out.println("총 저장된 첨부파일 " + foundPersonalSchedule.getAttachments().size());
        for (PersonalScheduleAttachment attachment : foundPersonalSchedule.getAttachments()) {
            attachment.setPersonalSchedule(foundPersonalSchedule);
        }
//        System.out.println(foundPersonalSchedule.getAttachments().get(0).getPersonalSchedule().getId());
        // 수정된 개인 일정 저장
        personalScheduleRepository.save(foundPersonalSchedule);
        return foundPersonalSchedule;
    }


    private void saveAttachment(Member member, PersonalSchedule findPersonalSchedule, MultipartFile attachment) throws IOException {
        URL savedUrl = s3Service.uploadPersonalScheduleFile(attachment, member.getId(), findPersonalSchedule.getId(), 0);
        PersonalScheduleAttachment personalScheduleAttachment = new PersonalScheduleAttachment();
        personalScheduleAttachment.setFileName(attachment.getOriginalFilename());
        personalScheduleAttachment.setFilePath(savedUrl.toString());
        personalScheduleAttachment.setPersonalSchedule(findPersonalSchedule);
        findPersonalSchedule.getAttachments().add(personalScheduleAttachment);
        System.out.println("총 저장된 첨부파일 " + findPersonalSchedule.getAttachments().size());
    }

    /**
     * 13. 개인 일정 삭제
     */
    public void deletePersonalScheduleById(String memberId, Long personalScheduleId) {
        Member foundMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

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
