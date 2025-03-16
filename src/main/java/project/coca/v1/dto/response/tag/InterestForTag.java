package project.coca.v1.dto.response.tag;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.coca.domain.tag.Interest;

@Builder
@Getter
@Setter
public class InterestForTag {
    private Long tagId;
    private String TagName;

    public static InterestForTag of(Interest interest) {
        return InterestForTag.builder()
                .tagId(interest.getTag().getId())
                .TagName(interest.getTag().getName())
                .build();
    }
}
