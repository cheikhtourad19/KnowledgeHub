package tn.esprim.knowledgehub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.entity.Reset;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ResetRepository extends JpaRepository<Reset,Long> {
    Optional<Reset> findByCode(String code);
    Optional<Reset> findByAuthUser(AuthUser authUser);

    void deleteByAuthUser(AuthUser authUser);

    List<Reset> findByExpiresAtAfter(LocalDateTime now);

    List<Reset> findByExpiresAtBefore(LocalDateTime now);

}
