package project.coca.domain.group;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
public class GroupNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GROUP_NOTICE_ID")
    private Long id;

    @Column(name = "CONTENTS", length = 500, nullable = false)
    private String contents;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CoGroup", nullable = false)
    private CoGroup coGroup;
}
