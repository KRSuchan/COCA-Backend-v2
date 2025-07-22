package project.coca.schedule.personal.dto;

import lombok.Builder;
import lombok.Data;
import project.coca.schedule.personal.PersonalScheduleAttachment;

@Data
@Builder
public class PersonalScheduleAttachmentResponse {
    private Long id;
    private String fileName;
    private String filePath;

    public static PersonalScheduleAttachmentResponse of(PersonalScheduleAttachment attachment) {
        return PersonalScheduleAttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .filePath(attachment.getFilePath())
                .build();
    }
}
