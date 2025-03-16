package project.coca.v1.dto.response.friend;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.personal.Friend;

@Builder
@Data
public class FriendResponse {
    private Long friendId; // 친구관계 고유 아이디
    private String friendMemberId;
    private String friendName;
    private String friendProfileImagePath;

    public static FriendResponse of(Friend friend) {
        return FriendResponse.builder()
                .friendId(friend.getId())
                .friendMemberId(friend.getOpponent().getId())
                .friendName(friend.getOpponentNickname())
                .friendProfileImagePath(friend.getOpponent().getProfileImgPath())
                .build();
    }
}
