package project.coca.v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import project.coca.domain.group.CoGroup;
import project.coca.domain.group.GroupManager;
import project.coca.domain.personal.Member;

import java.util.Optional;

public interface GroupManagerRepository extends JpaRepository<GroupManager, Long> {
    @Modifying // 해당 어노테이션이 없으면 Spring Data Jpa 에서 Select 쿼리라 생각함.
    @Query("DELETE FROM GroupManager gm WHERE gm.groupManager.id = :managerId AND gm.coGroup.id = :groupId")
    void deleteByManagerIdAndGroupId(String managerId, Long groupId);

    @Query("select gm from GroupManager gm where gm.groupManager.id = :memberId and gm.coGroup.id = :groupId")
    Optional<GroupManager> checkUserIsManager(String memberId, Long groupId);

    boolean existsByGroupManagerAndCoGroup(Member member, CoGroup coGroup);

}
