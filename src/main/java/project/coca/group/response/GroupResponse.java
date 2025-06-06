package project.coca.group.response;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.group.CoGroup;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class GroupResponse {
    private Long id;
    private String name;
    private Boolean isPrivate;
    private List<GroupTagResponse> groupTags;
    private Integer memberCount;

    public static GroupResponse of(CoGroup group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .isPrivate(group.getPrivatePassword() != null)
                .groupTags(group.getGroupTags().stream()
                        .map(GroupTagResponse::of)
                        .collect(Collectors.toList()))
                .memberCount(group.getGroupMembers().size())
                .build();
    }
}
