package project.coca.v1.dto.response.group;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.group.GroupMember;

@Builder
@Data
public class GroupMemberResponse {
    private String id; // 회원 id
    private String userName;
    private String profileImgPath;

    public static GroupMemberResponse of(GroupMember groupMember) {
        return GroupMemberResponse.builder()
                .id(groupMember.getGroupMember().getId())
                .userName(groupMember.getGroupMember().getUserName())
                .profileImgPath(groupMember.getGroupMember().getProfileImgPath())
                .build();
    }
}
