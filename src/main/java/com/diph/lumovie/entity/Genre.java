package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity @Table(name = "genres")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Genre extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(unique = true, nullable = false) private String name;
    private String slug;
    private String icon;
    @ManyToMany(mappedBy = "genres") private List<Movie> movies;
}
