package project.coca.v1.dto.response.tag;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.tag.Tag;

@Data
@Builder
public class TagResponse {
    private Long id;
    private String field;
    private String name;

    public static TagResponse of(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .field(tag.getField())
                .name(tag.getName())
                .build();
    }
}
