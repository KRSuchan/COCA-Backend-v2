package project.coca.group.response;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.tag.GroupTag;


@Data
@Builder
public class GroupTagResponse {
    private Long id;
    private String field;
    private String name;

    public static GroupTagResponse of(GroupTag tag) {
        return GroupTagResponse.builder()
                .id(tag.getTag().getId())
                .field(tag.getTag().getField())
                .name(tag.getTag().getName())
                .build();
    }
}
