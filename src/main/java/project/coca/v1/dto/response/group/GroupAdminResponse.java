package project.coca.v1.dto.response.group;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.group.CoGroup;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class GroupAdminResponse {
    private Long groupId;
    private String name;
    private String description;
    private String privatePassword;
    private List<GroupTagResponse> groupTags;
    private List<GroupMemberResponse> groupMembers;
    private List<GroupManagerResponse> groupManagers;
    private String groupNotice;

    public static GroupAdminResponse of(CoGroup group) {
        return GroupAdminResponse.builder()
                .groupId(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .privatePassword(group.getPrivatePassword())
                .groupTags(group.getGroupTags().stream()
                        .map(GroupTagResponse::of)
                        .collect(Collectors.toList()))
                .groupMembers(group.getGroupMembers().stream()
                        .map(GroupMemberResponse::of)
                        .collect(Collectors.toList()))
                .groupManagers(group.getGroupManager().stream()
                        .map(GroupManagerResponse::of)
                        .collect(Collectors.toList()))
                .groupNotice(group.getGroupNotice() == null ? null : group.getGroupNotice().getContents())
                .build();
    }
}
