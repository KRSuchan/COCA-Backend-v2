package project.coca.domain.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import project.coca.domain.personal.Member;
import project.coca.domain.request.GroupRequest;
import project.coca.domain.tag.GroupTag;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
public class CoGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_ID")
    private Long id;
    @Column(name = "NAME", length = 20, nullable = false)
    private String name;
    @Column(name = "DESCRIPTION", length = 500, nullable = false)
    private String description;
    @Column(name = "PRIVATE_PASSWORD", length = 16)
    private String privatePassword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ADMIN_ID", nullable = false)
    private Member admin;

    @OneToMany(mappedBy = "coGroup", cascade = CascadeType.ALL)
    private List<GroupManager> groupManager = new ArrayList<>();

    @OneToMany(mappedBy = "coGroup", cascade = CascadeType.ALL)
    private List<GroupMember> groupMembers = new ArrayList<>();

    @OneToOne(mappedBy = "coGroup", cascade = CascadeType.ALL)
    private GroupNotice groupNotice;

    @OneToMany(mappedBy = "coGroup", cascade = CascadeType.ALL)
    private List<GroupSchedule> groupSchedule = new ArrayList<>();

    @OneToMany(mappedBy = "coGroup", cascade = CascadeType.ALL)
    private List<GroupRequest> groupRequests = new ArrayList<>();

    @OneToMany(mappedBy = "coGroup", cascade = CascadeType.ALL)
    private List<GroupTag> groupTags = new ArrayList<>();
}
