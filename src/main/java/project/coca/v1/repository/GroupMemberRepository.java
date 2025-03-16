package project.coca.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.coca.domain.group.CoGroup;
import project.coca.domain.group.GroupMember;
import project.coca.domain.personal.Member;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    @Query("select m from GroupMember m where m.coGroup.id = :groupId and m.groupMember.id = :memberId")
    Optional<GroupMember> checkMemberInGroup(Long groupId, String memberId);

    void deleteGroupMemberByCoGroupAndGroupMember(CoGroup coGroup, Member member);

    // 파라미터 : Member 클래스의 groupMember 임.(네이밍 오류..)
    List<GroupMember> findAllByCoGroup(CoGroup group);

    Optional<GroupMember> findByCoGroupAndGroupMember(CoGroup coGroup, Member member);
}
