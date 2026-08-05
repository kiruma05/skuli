package com.skuli.staff.internal.repository;

import com.skuli.staff.internal.domain.Admin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link Admin}.
 */
public interface AdminRepository extends JpaRepository<Admin, String> {

    Optional<Admin> findByUsername(String username);
}
