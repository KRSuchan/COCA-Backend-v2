package project.coca.request.dto;

import lombok.Builder;
import lombok.Data;
import project.coca.request.GroupRequest;
import project.coca.request.RequestStatus;

@Data
@Builder
public class GroupRequestResponse {
    private Long groupRequestId;
    private Long groupId;
    private String groupName;
    private RequestMemberResponse sender;
    private RequestStatus status;

    public static GroupRequestResponse of(GroupRequest groupRequest) {
        return GroupRequestResponse.builder()
                .groupRequestId(groupRequest.getId())
                .groupName(groupRequest.getCoGroup().getName())
                .groupId(groupRequest.getCoGroup().getId())
                .sender(RequestMemberResponse.of(groupRequest.getSender()))
                .status(groupRequest.getRequestStatus())
                .build();
    }
}
