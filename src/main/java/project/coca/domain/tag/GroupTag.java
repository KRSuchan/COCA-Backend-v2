package project.coca.domain.tag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import project.coca.domain.group.CoGroup;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
public class GroupTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_TAG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_ID", nullable = false)
    private CoGroup coGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TAG_ID", nullable = false)
    private Tag tag;
}
