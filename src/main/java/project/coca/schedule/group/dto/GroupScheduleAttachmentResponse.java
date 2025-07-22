package project.coca.schedule.group.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.coca.schedule.group.GroupScheduleAttachment;

@Builder
@Getter
@Setter
public class GroupScheduleAttachmentResponse {
    private String fileName;
    private String filePath;

    public static GroupScheduleAttachmentResponse of(GroupScheduleAttachment attachment) {
        return GroupScheduleAttachmentResponse.builder()
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .build();
    }
}
