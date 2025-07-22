package project.coca.request.dto;

import lombok.Builder;
import lombok.Data;
import project.coca.request.FriendRequest;
import project.coca.request.RequestStatus;

@Data
@Builder
public class FriendRequestResponse {
    private Long friendRequestId;
    private RequestMemberResponse sender;
    private RequestStatus status;

    public static FriendRequestResponse of(FriendRequest friendRequest) {
        return FriendRequestResponse.builder()
                .friendRequestId(friendRequest.getId())
                .sender(RequestMemberResponse.of(friendRequest.getSender()))
                .status(friendRequest.getRequestStatus())
                .build();
    }
}
