package tn.esprim.knowledgehub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprim.knowledgehub.auth.entity.AuthUser;
import tn.esprim.knowledgehub.auth.entity.Reset;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ResetRepository extends JpaRepository<Reset,Long> {
    List<Reset> findByCode(String code);
    Optional<Reset> findByAuthUser(AuthUser authUser);

    @Modifying
    @Query("DELETE FROM Reset r WHERE r.authUser = :authUser")
    void deleteByAuthUser(@Param("authUser") AuthUser authUser);

    List<Reset> findByExpiresAtAfter(LocalDateTime now);

    List<Reset> findByExpiresAtBefore(LocalDateTime now);


    Optional<Reset> findByCodeAndExpiresAtAfter(String code, LocalDateTime dateTime);

    @Query("SELECT r FROM Reset r WHERE r.code = :code AND r.authUser.email = :email AND r.expiresAt > :dateTime")
    Optional<Reset> findByCodeAndEmailAndExpiresAtAfter(@Param("code") String code, @Param("email") String email, @Param("dateTime") LocalDateTime dateTime);


}
