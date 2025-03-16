package project.coca.v1.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindEmptyScheduleRequest {
    private List<String> members = new ArrayList<>();
    private LocalDate startDate; //찾기를 시작하는 날짜 (최대 1년까지 찾어요...)
    private LocalDate endDate; //찾기를 끝내는 날짜 (start~end 사이에서 찾음)
    private int findDay;
    private int findMinute; //시간도 분으로 바꿔서 주세요
}
