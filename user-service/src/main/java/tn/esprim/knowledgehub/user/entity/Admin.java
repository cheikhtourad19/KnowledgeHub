package tn.esprim.knowledgehub.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Admin {
    @Id
    private Long id;
    private String authId;
}
