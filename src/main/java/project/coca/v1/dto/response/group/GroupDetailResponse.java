package project.coca.v1.dto.response.group;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.group.CoGroup;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class GroupDetailResponse {
    private Long id;
    private String name;
    private AdminResponse admin;
    private String description;
    private Boolean isPrivate;
    private List<GroupTagResponse> groupTags;
    private GroupNoticeResponse notice;

    public static GroupDetailResponse of(CoGroup group) {
        return GroupDetailResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .admin(AdminResponse.of(group.getAdmin()))
                .description(group.getDescription())
                .isPrivate(group.getPrivatePassword() != null)
                .groupTags(group.getGroupTags().stream()
                        .map(GroupTagResponse::of)
                        .collect(Collectors.toList()))
                .notice(GroupNoticeResponse.of(group.getGroupNotice()))
                .build();
    }
}
