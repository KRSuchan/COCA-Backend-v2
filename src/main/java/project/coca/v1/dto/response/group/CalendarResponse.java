package project.coca.v1.dto.response.group;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.group.CoGroup;
import project.coca.domain.personal.Member;

@Data
@Builder
public class CalendarResponse {
    private Long groupId;
    private String groupName;
    private Boolean isAdmin;
    private Boolean isManager;

    public static CalendarResponse of(CoGroup group, Member member) {
        return CalendarResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .isAdmin(group.getAdmin().getId().equals(member.getId()))
                .isManager(group.getGroupManager().stream().anyMatch(groupManager -> groupManager.getId().equals(group.getId())))
                .build();
    }
}
