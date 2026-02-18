package tn.esprim.knowledgehub.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprim.knowledgehub.user.entity.Admin;
import tn.esprim.knowledgehub.user.repository.AdminRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;

    @Transactional
    public Admin createAdmin(String authId) {
        log.info("Creating admin with authId: {}", authId);

        // Check if admin already exists
        if (adminRepository.findByAuthId(authId).isPresent()) {
            log.warn("Admin already exists for authId: {}", authId);
            throw new RuntimeException("Admin already exists for authId: " + authId);
        }

        Admin admin = Admin.builder()
                .authId(authId)
                .build();

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Admin created successfully with ID: {}", savedAdmin.getId());

        return savedAdmin;
    }

    public Admin getAdminByAuthId(String authId) {
        return adminRepository.findByAuthId(authId)
                .orElseThrow(() -> new RuntimeException("Admin not found for authId: " + authId));
    }

    public void deleteAdmin(Long id) {
        log.info("Deleting admin with id: {}", id);
        adminRepository.deleteById(id);
    }
}
