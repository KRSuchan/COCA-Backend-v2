package project.coca.group.dto;

import lombok.Getter;
import lombok.Setter;
import project.coca.group.CoGroup;
import project.coca.member.Member;
import project.coca.tag.GroupTag;

import java.util.List;

@Getter
@Setter
public class GroupRequest {
    private Member member;
    private CoGroup group;
    private List<GroupTag> groupTags;
}
