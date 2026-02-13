package tn.esprim.knowledgehub.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprim.knowledgehub.user.entity.Admin;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,Long> {
   Optional<Admin> findByAuthId(String authId);
}
