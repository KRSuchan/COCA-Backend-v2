package project.coca.group.dto;

import lombok.Builder;
import lombok.Data;
import project.coca.group.GroupManager;

@Builder
@Data
public class GroupManagerResponse {
    private String id;
    private String userName;
    private String profileImgPath;

    public static GroupManagerResponse of(GroupManager groupManager) {
        return GroupManagerResponse.builder()
                .id(groupManager.getGroupManager().getId())
                .userName(groupManager.getGroupManager().getUserName())
                .profileImgPath(groupManager.getGroupManager().getProfileImgPath())
                .build();
    }
}
