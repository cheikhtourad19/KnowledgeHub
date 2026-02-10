package tn.esprim.knowledgehub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprim.knowledgehub.auth.entity.AuthUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email); // Email is the username

    boolean existsByEmail(String email);

    /**
     * Batch query to find all existing emails from a list
     * Used for bulk user creation to avoid N+1 queries
     */
    @Query("SELECT u.email FROM AuthUser u WHERE u.email IN :emails")
    List<String> findEmailsByEmailIn(@Param("emails") List<String> emails);

}