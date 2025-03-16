package project.coca.v1.dto.response.request;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.request.GroupRequest;
import project.coca.domain.request.RequestStatus;

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
