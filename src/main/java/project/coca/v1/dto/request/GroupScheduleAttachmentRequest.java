package project.coca.v1.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
public class GroupScheduleAttachmentRequest {
    private String fileName;
    private String fileType;
    private MultipartFile file;
}
