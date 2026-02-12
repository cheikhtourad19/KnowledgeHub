package tn.esprim.knowledgehub.user.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @OneToMany(mappedBy = "classe", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Student> students;
}
