package tn.esprim.knowledgehub.user.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
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
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Teacher> encandrants;


}
