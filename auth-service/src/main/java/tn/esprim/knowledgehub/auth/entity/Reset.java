package tn.esprim.knowledgehub.auth.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auth_resets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "auth_user_id", nullable = false)
    private AuthUser authUser;

    private LocalDateTime expiresAt;

    private String code;
}

