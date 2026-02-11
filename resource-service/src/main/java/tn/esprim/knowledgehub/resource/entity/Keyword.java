package tn.esprim.knowledgehub.resource.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@Entity
public class Keyword {
    @Id
    private Long id;

    private String keyword;

}
