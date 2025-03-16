package project.coca.v1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.oxm.ValidationFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.coca.domain.group.CoGroup;
import project.coca.domain.group.GroupManager;
import project.coca.domain.group.GroupMember;
import project.coca.domain.group.GroupNotice;
import project.coca.domain.personal.Member;
import project.coca.domain.tag.GroupTag;
import project.coca.domain.tag.Tag;
import project.coca.v1.dto.response.common.error.AlreadyReportedException;
import project.coca.v1.dto.response.group.GroupDetailSearchResponse;
import project.coca.v1.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class GroupService {
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupManagerRepository groupManagerRepository;
    private final TagRepository tagRepository;
    private final GroupTagRepository groupTagRepository;
    private final GroupNoticeRepository groupNoticeRepository;

    private Integer pageSize = 8;

    @Autowired
    public GroupService(GroupRepository groupRepository,
                        MemberRepository memberRepository,
                        GroupMemberRepository groupMemberRepository,
                        GroupManagerRepository groupManagerRepository,
                        TagRepository tagRepository, GroupTagRepository groupTagRepository,
                        GroupNoticeRepository groupNoticeRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupManagerRepository = groupManagerRepository;
        this.tagRepository = tagRepository;
        this.groupTagRepository = groupTagRepository;
        this.groupNoticeRepository = groupNoticeRepository;
    }

    /**
     * 07. 캘린더 목록 조회
     */
    public List<CoGroup> findJoinedGroups(String memberId) {
        // 1. 회원 검증
        memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        // 2. 회원이 참가중인 그룹 목록 조회
        return groupRepository.findByGroupMemberId(memberId);
    }

    /**
     * a1. 그룹 참가하기
     */
    public void joinGroup(Member member, CoGroup group) {
        // 1. 회원 검증
        Member findMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        // 2. 그룹 조회
        CoGroup findGroup = groupRepository.findById(group.getId())
                .orElseThrow(() -> new NoSuchElementException("그룹이 조회되지 않습니다."));
        // 3. 이미 참가중인가
        List<CoGroup> findJoinedGroups = groupRepository.findByGroupMemberId(findMember.getId());
        if (findJoinedGroups.contains(findGroup))
            throw new AlreadyReportedException("이미 참가중입니다.");
        // 그룹 패스워드 일치 여부
        if (findGroup.getPrivatePassword() == null
                || findGroup.getPrivatePassword().equals(group.getPrivatePassword())) {
            // 3. 회원 그룹 데이터를 groupMember에 넣기
            GroupMember groupMember = new GroupMember();
            groupMember.setGroupMember(findMember);
            groupMember.setCoGroup(findGroup);
            // 4. 그룹 회원 등록
            groupMemberRepository.save(groupMember);
        } else {
            throw new ValidationFailureException("비밀번호가 잘못되었습니다.");
        }
    }

    /**
     * a2. 그룹에서 탈퇴
     */
    public void leaveGroup(String memberId, Long groupId) {
        // 1. 회원 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        // 2. 그룹 조회
        CoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 조회되지 않습니다."));
        if (group.getAdmin().equals(member)) {
            // 그룹 관리자는 그룹 탈퇴 불가
            throw new ValidationFailureException("그룹 관리자는 탈퇴할 수 없습니다.");
        }
        if (group.getGroupManager().contains(member)) {
            // 그룹 매니저는 매니저 권한 반납 후 탈퇴
            groupManagerRepository.deleteByManagerIdAndGroupId(member.getId(), group.getId());
        }
        // 3. 그룹 멤버 삭제 수행 (그룹에서 탈퇴)
        groupMemberRepository.deleteGroupMemberByCoGroupAndGroupMember(group, member);
    }

    /**
     * 21. 그룹 등록
     */
    public CoGroup saveGroup(Member creator, CoGroup group, List<GroupTag> groupTags) {
        // 회원 검증
        Member admin = memberRepository.findById(creator.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        // 그룹에 admin 회원 반영하고 그룹 생성
        group.setAdmin(admin);
        CoGroup savedGroup = groupRepository.save(group);

        // 관리자를 그룹 멤버에 등록하기
        GroupMember groupMember = new GroupMember();
        groupMember.setCoGroup(savedGroup);
        groupMember.setGroupMember(admin);
        groupMemberRepository.save(groupMember);

        // 관리자를 그룹 매니저에 등록하기
        GroupManager groupManager = new GroupManager();
        groupManager.setCoGroup(savedGroup);
        groupManager.setGroupManager(admin);
        groupManagerRepository.save(groupManager);

        // 그룹 태그가 있는 경우
        if (groupTags != null && !groupTags.isEmpty()) {
            for (GroupTag groupTag : groupTags) {
                groupTag.setCoGroup(savedGroup);
                Tag tag = tagRepository.findById(groupTag.getTag().getId()).get();
                groupTag.setTag(tag);
            }
            savedGroup.setGroupTags(groupTags);
        }
        // 최종 리턴
        return groupRepository.findById(savedGroup.getId()).orElseThrow();
    }

    /**
     * 22-1. 그룹 검색 by 그룹명
     */
    public Page<CoGroup> findGroupsByNameLike(String groupName, Integer pageNumber) {
        // 페이지 세팅
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        try {
            // 페이징한 데이터 가져오기
            Page<CoGroup> resultPage = groupRepository
                    .findByNameContainingOrderByGroupMembersDesc(groupName, pageable);
            return resultPage;
        } catch (NoSuchElementException e) {
            return Page.empty();
        }
    }

    /**
     * 22-2. 그룹 검색 by 태그
     */
    public Page<CoGroup> findGroupsByTag(String tagName, Integer pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        try {
            Tag tag = tagRepository.findByName(tagName).orElseThrow(() -> new NoSuchElementException("태그가 조회되지 않습니다."));
            Page<CoGroup> resultPage = groupRepository
                    .findByTagNameOrderByGroupMembersDesc(tag.getName(), pageable);
            return resultPage;
        } catch (NoSuchElementException e) {
            return Page.empty();
        }
    }

    /**
     * 23. 그룹 상세 정보 조회
     */
    public GroupDetailSearchResponse findGroupById(String memberId, Long groupId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        CoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 조회되지 않습니다."));
        return GroupDetailSearchResponse.of(group, member);
    }

    /**
     * 25. 그룹 수정
     */
    public void updateGroup(Member admin,
                            CoGroup updateGroup,
                            List<Tag> groupTags,
                            GroupNotice notice,
                            List<Member> membersToManager,
                            List<Member> managersToMember) {
        // 1. 그룹 조회
        CoGroup findGroup = groupRepository.findById(updateGroup.getId())
                .orElseThrow(() -> new NoSuchElementException("그룹이 조회되지 않습니다."));
        System.out.println("그룹조회완");
        // 2. admin 인가?
        Member member = memberRepository.findById(admin.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        System.out.println("admin 조회 완");
        if (!findGroup.getAdmin().equals(member)) {
            throw new ValidationFailureException("수정 권한이 없습니다.");
        }
        System.out.println("수정 권한 확인 완");
        // 3. 조회된 그룹 수정
        findGroup.setName(updateGroup.getName());
        findGroup.setDescription(updateGroup.getDescription());
        findGroup.setGroupManager(updateGroup.getGroupManager());

        // 일반회원에서 매니저로 변경
        for (Member toManager : membersToManager) {
            // 해당 회원이 이미 매니저인지 확인
            boolean isManager = groupManagerRepository.existsByGroupManagerAndCoGroup(toManager, findGroup);
            // 이미 매니저인 경우에는 제외
            if (!isManager) {
                GroupManager newManager = new GroupManager();
                newManager.setCoGroup(findGroup);
                newManager.setGroupManager(toManager);
                groupManagerRepository.save(newManager);
            }
        }
        System.out.println("일반회원에서 매니저로 변경 완");
        // 매니저에서 일반회원으로 변경
        for (Member toMember : managersToMember) {
            if (!admin.getId().equals(toMember.getId())) {
                groupManagerRepository.deleteByManagerIdAndGroupId(toMember.getId(), findGroup.getId());
            }
        }
        System.out.println("매니저에서 일반회원으로 변경 완");
        // 4. 그룹 태그 모두 삭제 후 재세팅
        groupTagRepository.deleteAllByCoGroupId(findGroup.getId());
        List<GroupTag> newGroupTags = new ArrayList<>();
        for (Tag groupTag : groupTags) {
            GroupTag newGroupTag = new GroupTag();
            newGroupTag.setCoGroup(findGroup);
            Tag tag = tagRepository.findById(groupTag.getId())
                    .orElseThrow(() -> new NoSuchElementException("태그가 조회되지 않습니다."));
            newGroupTag.setTag(tag);
            newGroupTags.add(newGroupTag);
        }
        findGroup.setGroupTags(newGroupTags);
        System.out.println("그룹 태그 모두 삭제 후 재세팅 완");
        // 5. 조회된 그룹의 공지 등록 / 수정 / 삭제
        GroupNotice existingNotice = findGroup.getGroupNotice();
        if (notice != null && notice.getContents() != null) {
            if (existingNotice != null) {
                // 기존 공지가 있는 경우 업데이트
                existingNotice.setContents(notice.getContents());
                existingNotice.setCoGroup(findGroup);
                groupNoticeRepository.save(existingNotice);
            } else {
                // 새로운 공지 추가
                notice.setCoGroup(findGroup);
                groupNoticeRepository.save(notice);
                findGroup.setGroupNotice(notice);
            }
        } else {
            if (existingNotice != null) {
                // 공지가 없는 경우 기존 공지 삭제
                groupNoticeRepository.delete(existingNotice);
                findGroup.setGroupNotice(null);
            }
        }
        System.out.println("조회된 그룹의 공지 등록 / 수정 / 삭제 완");
        // 6. 수정된 그룹 저장
        groupRepository.save(findGroup);
    }

    /**
     * 25-a. Admin을 위한 그룹 상세 정보 조회
     */
    public CoGroup findGroupForAdmin(CoGroup group, Member admin) {
        // 1. 회원 조회
        Member findMember = memberRepository.findById(admin.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        // 2. 그룹 조회
        CoGroup findGroup = groupRepository.findById(group.getId())
                .orElseThrow(() -> new NoSuchElementException("그룹이 조회되지 않습니다."));
        // 3. 권한 검증 / 최종 : 그룹 리턴
        if (!findGroup.getAdmin().getId().equals(admin.getId())) {
            // admin id 불일치
            throw new ValidationFailureException("조회 권한이 없습니다.");
        } else {
            return findGroup;
        }
    }

    /**
     * 26. 그룹 삭제
     */
    public void deleteGroup(String adminId, Long groupId) {
        CoGroup findGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 조회되지 않습니다."));
        if (findGroup.getAdmin().getId().equals(adminId)) {
            groupRepository.deleteById(findGroup.getId());
        } else {
            throw new ValidationFailureException("삭제 권한이 없습니다.");
        }
    }

    /**
     * 28. 그룹 공지 조회
     */
    public GroupNotice findGroupNotice(String memberId, Long groupId) {
        CoGroup findGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 조회되지 않습니다."));
        if (findGroup.getGroupMembers().stream()
                .noneMatch(groupMember -> groupMember.getGroupMember().getId()
                        .equals(memberId))) {
            throw new ValidationFailureException("참가중이지 않습니다.");
        }
        return findGroup.getGroupNotice();
    }

    /**
     * a. 그룹 회원 목록 조회
     */
    public List<GroupMember> findGroupMembers(String memberId, Long groupId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        CoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NoSuchElementException("그룹이 조회되지 않습니다."));
        groupMemberRepository.checkMemberInGroup(group.getId(), member.getId())
                .orElseThrow(() -> new NoSuchElementException("그룹의 회원이 아닙니다."));
        return groupMemberRepository.findAllByCoGroup(group);
    }
}
