package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "actors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Actor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "actorLinks")
    @Builder.Default
    private Set<Movie> movies = new HashSet<>();
}
