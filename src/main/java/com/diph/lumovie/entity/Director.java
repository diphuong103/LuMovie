package com.diph.lumovie.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "directors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Director extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "directorLinks")
    @Builder.Default
    private Set<Movie> movies = new HashSet<>();
}
