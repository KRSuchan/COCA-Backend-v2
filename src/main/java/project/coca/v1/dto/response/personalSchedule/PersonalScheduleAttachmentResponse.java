package project.coca.v1.dto.response.personalSchedule;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.personal.PersonalScheduleAttachment;

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
