package project.coca.request.dto;

import lombok.Getter;
import lombok.Setter;
import project.coca.request.RequestStatus;

@Setter
@Getter
public class RequestUpdateRequest {
    private Long requestId;
    private RequestStatus status;
}
