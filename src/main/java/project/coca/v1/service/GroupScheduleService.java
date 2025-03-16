package project.coca.v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.coca.domain.group.*;
import project.coca.domain.personal.Member;
import project.coca.domain.personal.PersonalSchedule;
import project.coca.domain.personal.PersonalScheduleAttachment;
import project.coca.v1.dto.request.GroupScheduleRequest;
import project.coca.v1.repository.*;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@Service
@Transactional
@RequiredArgsConstructor
public class GroupScheduleService {
    @Autowired
    private final GroupScheduleRepository groupScheduleRepository;
    @Autowired
    private final GroupMemberRepository groupMemberRepository;
    @Autowired
    private final GroupManagerRepository groupManagerRepository;
    @Autowired
    private final GroupScheduleAttachmentRepository groupScheduleAttachmentRepository;
    @Autowired
    private final GroupRepository groupRepository;
    @Autowired
    private final MemberRepository memberRepository;
    @Autowired
    private final PersonalScheduleRepository personalScheduleRepository;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private GroupScheduleHeartRepository groupScheduleHeartRepository;

    //파일의 md5 생성
    public String generateFileMd5(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] fileByte = file.getBytes();

        md.update(fileByte);
        byte[] hash = md.digest();

        return new BigInteger(1, hash).toString(16) + file.getName();
    }

    private GroupScheduleAttachment generateAttachment(
            MultipartFile multipartFile, GroupSchedule schedule, int divisionNum)
            throws NoSuchAlgorithmException, IOException {
        GroupScheduleAttachment changeAttach = new GroupScheduleAttachment();

        changeAttach.setFileName(multipartFile.getOriginalFilename());
        // 그룹 일정 첨부파일 aws 코드
        URL url = s3Service.uploadGroupScheduleFile(multipartFile, schedule.getCoGroup().getId(), schedule.getId(), divisionNum);
        changeAttach.setFilePath(url.toString());

        changeAttach.setFileMd5(generateFileMd5(multipartFile));
        changeAttach.setGroupSchedule(schedule);

        return changeAttach;
    }

    /* 그룹 일정 목록 조회 & 그룹 일정 상세 정보 조회
    멤버가 그룹에 포함되어있는지 확인
        -> 그룹 일정 조회
        -> 일정 반환
    */
    public List<GroupSchedule> groupScheduleInquiry(
            Long groupId, String memberId, LocalDate startDay, LocalDate endDay) {
        GroupMember checkMember = groupMemberRepository.checkMemberInGroup(groupId, memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 그룹에 속해있지 않습니다."));

        LocalDateTime startDate = startDay.atStartOfDay();
        LocalDateTime endDate = endDay.atTime(LocalTime.of(23, 59, 59));

        return groupScheduleRepository.findGroupSchedule(groupId, startDate, endDate);
    }

    /* 그룹 일정 등록
    그룹이 존재하는지 확인 & 신청한 멤버가 관리자인지 확인 -> 등록
    파일 저장하는 로직 추가 필요
     */
    public GroupSchedule groupScheduleRegistrationReq(GroupScheduleRequest requestSchedule, MultipartFile[] files) throws NoSuchAlgorithmException, IOException {
        CoGroup group = groupRepository.findById(requestSchedule.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("그룹이 존재하지 않습니다."));

        GroupManager checkUser = groupManagerRepository.checkUserIsManager(requestSchedule.getMemberId(), requestSchedule.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("해당 그룹의 관리자가 아닙니다."));

        GroupSchedule registSchedule = new GroupSchedule();

        registSchedule.setCoGroup(group);
        registSchedule.setTitle(requestSchedule.getTitle());
        registSchedule.setDescription(requestSchedule.getDescription());
        registSchedule.setLocation(requestSchedule.getLocation());
        registSchedule.setStartTime(requestSchedule.getStartTime());
        registSchedule.setEndTime(requestSchedule.getEndTime());
        registSchedule.setColor(requestSchedule.getColor());

        // 그룹 일정 id를 얻기 위해 한 번 save 해야함
        registSchedule = groupScheduleRepository.save(registSchedule);

        List<GroupScheduleAttachment> attachments = new ArrayList<>();
        if (files != null && files.length > 0)
            for (int i = 0; i < files.length; i++) {
                if (files[i] != null) // 논리 AND 조건으로 수정
                    attachments.add(generateAttachment(files[i], registSchedule, i));
            }
        registSchedule.setGroupScheduleAttachments(attachments);

        return groupScheduleRepository.save(registSchedule);
    }

    /* 그룹 일정 수정
    일정이 존재하는지 확인 & 신청한 멤버가 관리자인지 확인 -> 등록
    파일 저장하는 로직 추가 필요
     */
    public GroupSchedule groupScheduleUpdate(GroupScheduleRequest requestSchedule, MultipartFile[] files)
            throws NoSuchAlgorithmException, IOException {
        GroupManager checkUser = groupManagerRepository.checkUserIsManager(requestSchedule.getMemberId(), requestSchedule.getGroupId())
                .orElseThrow(() -> new NoSuchElementException("해당 그룹의 관리자가 아닙니다."));

        GroupSchedule updateSchedule = groupScheduleRepository.findById(requestSchedule.getScheduleId())
                .orElseThrow(() -> new NoSuchElementException("일정이 조회되지 않습니다."));

        updateSchedule.setTitle(requestSchedule.getTitle());
        updateSchedule.setDescription(requestSchedule.getDescription());
        updateSchedule.setLocation(requestSchedule.getLocation());
        updateSchedule.setStartTime(requestSchedule.getStartTime());
        updateSchedule.setEndTime(requestSchedule.getEndTime());
        updateSchedule.setColor(requestSchedule.getColor());

        List<String> existAttachMD5s = new ArrayList<>();
        if (updateSchedule.getGroupScheduleAttachments() != null && updateSchedule.getGroupScheduleAttachments().size() > 0) {
            for (GroupScheduleAttachment attachment : updateSchedule.getGroupScheduleAttachments())
                existAttachMD5s.add(attachment.getFileMd5());
        }

        List<String> newAttachMD5s = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile multipartFile : files)
                if (multipartFile != null) {
                    newAttachMD5s.add(generateFileMd5(multipartFile));
                }
        }

        //복사해서 바꿔야 오류가 안납니다...
        List<GroupScheduleAttachment> attachmentsCopy = new ArrayList<>(updateSchedule.getGroupScheduleAttachments());

        //새로운거에 없음 -> 기존 DB에서 삭제
        if (attachmentsCopy != null && attachmentsCopy.size() > 0) {
            for (GroupScheduleAttachment attachment : attachmentsCopy) {
                if (!newAttachMD5s.contains(attachment.getFileMd5())) {
                    s3Service.deleteS3File(attachment.getFilePath());
                    updateSchedule.removeAttachment(attachment);
                    attachment.setGroupSchedule(null);
                    groupScheduleAttachmentRepository.delete(attachment);
                }
            }
            System.out.println("삭제삭제요");
            groupScheduleAttachmentRepository.flush();
        }

        //기존거에 없음 -> 기존거에 새로운거 추가
        if (newAttachMD5s != null && newAttachMD5s.size() > 0) {
            for (int i = 0; i < newAttachMD5s.size(); i++) {
                if (!existAttachMD5s.contains(newAttachMD5s.get(i))) {
                    GroupScheduleAttachment newAttach = generateAttachment(files[i], updateSchedule, i);
                    updateSchedule.addAttachment(newAttach);
                }
            }
        }
        return groupScheduleRepository.save(updateSchedule);
    }

    /* 그룹 일정 삭제
    해당 멤버가 관리자인지 확인
        -> 관리자라면 일정 조회
        -> 일정이 존재한다면 일정 삭제 -> 삭제 확인 후 true 반환. false는 일정 삭제 안된것.

        추후 aws에서 파일 삭제하는 로직도 추가해야함
    */
    public boolean groupScheduleDelete(Long groupId, Long scheduleId, String memberId) {
        GroupManager checkUser = groupManagerRepository.checkUserIsManager(memberId, groupId)
                .orElseThrow(() -> new NoSuchElementException("해당 그룹의 관리자가 아닙니다."));

        GroupSchedule deleteSchedule = groupScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("일정이 조회되지 않습니다."));

        if (deleteSchedule.getGroupScheduleAttachments() != null && deleteSchedule.getGroupScheduleAttachments().size() > 0) {
            //aws에서 파일 지운 후 DB에서 삭제
            for (GroupScheduleAttachment attachment : deleteSchedule.getGroupScheduleAttachments())
                s3Service.deleteS3File(attachment.getFilePath());

            groupScheduleAttachmentRepository.deleteAll(deleteSchedule.getGroupScheduleAttachments());
        }

        groupScheduleRepository.delete(deleteSchedule);
        groupScheduleRepository.flush();

        if (groupScheduleRepository.existsById(scheduleId))
            return false;
        else
            return true;
    }

    /* 내 일정으로 가져오기(하트 버튼)
     멤버가 그룹 내의 회원인지 확인 -> 그룹의 일정을 멤버 개인 캘린더에 저장
    */
    public PersonalSchedule setGroupScheduleToPersonalSchedule(Long groupId, Long scheduleId, String memberId) {
        CoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 존재하지 않습니다."));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        GroupMember checkMember = groupMemberRepository.checkMemberInGroup(groupId, memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 그룹에 속해있지 않습니다."));

        GroupSchedule groupSchedule = groupScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("일정이 조회되지 않습니다."));

        PersonalSchedule personalSchedule = new PersonalSchedule();

        personalSchedule.setMember(member);
        personalSchedule.setTitle(group.getName() + "그룹의 일정 - " + groupSchedule.getTitle());
        personalSchedule.setDescription(groupSchedule.getDescription());
        personalSchedule.setLocation(groupSchedule.getLocation());
        personalSchedule.setStartTime(groupSchedule.getStartTime());
        personalSchedule.setEndTime(groupSchedule.getEndTime());
        personalSchedule.setColor(groupSchedule.getColor());
        personalSchedule.setIsPrivate(false);

        List<PersonalScheduleAttachment> attachments = new ArrayList<>();

        if (groupSchedule.getGroupScheduleAttachments() != null && groupSchedule.getGroupScheduleAttachments().size() > 0) {
            for (GroupScheduleAttachment attachment : groupSchedule.getGroupScheduleAttachments()) {
                PersonalScheduleAttachment newAttachment = new PersonalScheduleAttachment();
                newAttachment.setFilePath(attachment.getFilePath());
                newAttachment.setFileName(attachment.getFileName());
                newAttachment.setPersonalSchedule(personalSchedule);

                attachments.add(newAttachment);
            }
        }
        personalSchedule.setAttachments(attachments);
        personalSchedule = personalScheduleRepository.save(personalSchedule);
        // 하트 등록
        GroupScheduleHeart heart = new GroupScheduleHeart();
        heart.setGroupSchedule(groupSchedule);
        heart.setGroupMember(checkMember);
        groupScheduleHeartRepository.save(heart);

        return personalSchedule;
    }

    /* 내 일정 그룹일정에 통합
     멤버가 그룹 내의 회원인지 확인 -> 멤버 개인 일정을 그룹 일정으로 저장
    */
    public List<GroupSchedule> setPersonalScheduleToGroupSchedule(Long groupId, String memberId, LocalDate date) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다."));

        CoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 존재하지 않습니다."));

        GroupMember checkMember = groupMemberRepository.checkMemberInGroup(groupId, memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 그룹에 속해있지 않습니다."));

        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.atTime(LocalTime.of(23, 59, 59));

        List<PersonalSchedule> memberSchedule = personalScheduleRepository.findPersonalScheduleByDateRange(memberId, startDate, endDate);
        List<GroupSchedule> newSchedule = group.getGroupSchedule();

        if (memberSchedule != null && memberSchedule.size() > 0) {
            for (PersonalSchedule personalSchedule : memberSchedule) {
                GroupSchedule groupSchedule = new GroupSchedule();
                groupSchedule.setCoGroup(group);
                groupSchedule.setStartTime(personalSchedule.getStartTime());
                groupSchedule.setEndTime(personalSchedule.getEndTime());
                groupSchedule.setColor(personalSchedule.getColor());

                if (personalSchedule.getIsPrivate())
                    groupSchedule.setTitle(member.getUserName() + "의 비공개 일정");
                else
                    groupSchedule.setTitle(member.getUserName() + "의 일정: " + personalSchedule.getTitle());

                newSchedule.add(groupSchedule);
            }
        }
        group.setGroupSchedule(newSchedule);
        groupRepository.save(group);
        groupRepository.flush();

        return groupScheduleRepository.findGroupSchedule(groupId, startDate, endDate);
    }
}
