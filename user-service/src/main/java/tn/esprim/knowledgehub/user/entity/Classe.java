package tn.esprim.knowledgehub.user.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classeId;
    private String classeName;
    @OneToMany(mappedBy = "classe")
    @JsonIgnore  // Ignore the students list to avoid recursion when fetching Classe
    private List<Student> students;
}
