package tn.esprim.knowledgehub.user.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Builder
@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @NonNull
    private String authId;

    @NonNull
    private String nom;

    @NonNull
    private String prenom;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Teacher> encandrants = new ArrayList<>();
}
