package project.coca.request.dto;

import lombok.Builder;
import lombok.Data;
import project.coca.request.RequestStatus;
import project.coca.request.ScheduleRequest;

import java.time.LocalDateTime;

@Builder
@Data
public class ScheduleRequestResponse {
    private Long requestedScheduleId;
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private RequestMemberResponse sender;
    private RequestStatus status;

    public static ScheduleRequestResponse of(ScheduleRequest scheduleRequest) {
        return ScheduleRequestResponse.builder()
                .requestedScheduleId(scheduleRequest.getRequestedSchedule().getId())
                .title(scheduleRequest.getRequestedSchedule().getTitle())
                .start(scheduleRequest.getRequestedSchedule().getStartTime())
                .end(scheduleRequest.getRequestedSchedule().getEndTime())
                .sender(RequestMemberResponse.of(scheduleRequest.getSender()))
                .status(scheduleRequest.getRequestStatus())
                .build();
    }
}
