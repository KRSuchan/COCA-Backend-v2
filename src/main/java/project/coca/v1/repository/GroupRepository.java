package project.coca.v1.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import project.coca.domain.group.CoGroup;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<CoGroup, Long> {
    Optional<CoGroup> findById(Long groupId);

    // 그룹 멤버 수를 기준으로 오름차순으로 페이징 및 정렬된 결과를 반환하는 메서드
    @Query("SELECT g FROM CoGroup g WHERE g.name LIKE %:name% ORDER BY SIZE(g.groupMembers) DESC")
    Page<CoGroup> findByNameContainingOrderByGroupMembersDesc(String name, Pageable pageable);

    @Query("SELECT g FROM CoGroup g JOIN g.groupMembers gm WHERE gm.groupMember.id = :groupMemberId")
    List<CoGroup> findByGroupMemberId(String groupMemberId);

    @Query("SELECT g FROM CoGroup g JOIN g.groupTags t WHERE t.tag.name = :tagName ORDER BY SIZE(g.groupMembers) DESC")
    Page<CoGroup> findByTagNameOrderByGroupMembersDesc(String tagName, Pageable pageable);
}
