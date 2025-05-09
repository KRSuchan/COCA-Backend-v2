package project.coca.member.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.coca.domain.tag.Interest;

@Builder
@Getter
@Setter
public class InterestForTag {
    private Long tagId;
    private String tagName;

    public static InterestForTag of(Interest interest) {
        return InterestForTag.builder()
                .tagId(interest.getTag().getId())
                .tagName(interest.getTag().getName())
                .build();
    }
}
