package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "comments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Comment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne @JoinColumn(name = "movie_id", nullable = false) private Movie movie;
    @ManyToOne @JoinColumn(name = "parent_id") private Comment parent;
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL) private List<Comment> replies;
    @Column(columnDefinition = "TEXT", nullable = false) private String content;
    @Builder.Default
    private Integer likes = 0;
}
