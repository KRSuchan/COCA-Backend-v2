package project.coca.v1.dto.response.group;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.group.CoGroup;
import project.coca.domain.personal.Member;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class GroupDetailSearchResponse {
    private Long id;
    private String name;
    private AdminResponse admin;
    private String description;
    private Boolean isPrivate;
    private List<GroupTagResponse> groupTags;
    private Integer memberCount;
    private Boolean isAdmin;
    private Boolean isMember;

    public static GroupDetailSearchResponse of(CoGroup group, Member member) {
        return GroupDetailSearchResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .admin(AdminResponse.of(group.getAdmin()))
                .description(group.getDescription())
                .isPrivate(group.getPrivatePassword() != null)
                .groupTags(group.getGroupTags().stream()
                        .map(GroupTagResponse::of)
                        .collect(Collectors.toList()))
                .memberCount(group.getGroupMembers().size())
                .isAdmin(group.getAdmin().getId().equals(member.getId()))
                .isMember(group.getGroupMembers().stream()
                        .anyMatch(groupMember -> groupMember.getGroupMember().getId().equals(member.getId())))
                .build();
    }
}
