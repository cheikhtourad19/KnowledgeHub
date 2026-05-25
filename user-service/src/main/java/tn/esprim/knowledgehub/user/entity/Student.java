package tn.esprim.knowledgehub.user.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(indexes = @Index(name = "idx_student_auth_id", columnList = "authId"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Student extends UserBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;


    @JoinColumn(name = "classe_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("students")  // Ignore students list in classe to prevent infinite recursion
    private Classe classe;
}

