package project.coca.v1.dto.response.group;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.group.GroupNotice;

@Builder
@Data
public class GroupNoticeResponse {
    private String contents;

    public static GroupNoticeResponse of(GroupNotice notice) {
        return GroupNoticeResponse.builder()
                .contents(notice == null || notice.getContents() == null ? null : notice.getContents())
                .build();
    }
}
