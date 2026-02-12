package tn.esprim.knowledgehub.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class UserBase {


    @Column(nullable = false)
    protected String authId;

    @Column(nullable = false, unique = true, length = 150)
    protected String email;

    protected String nom;
    protected String prenom;
}
