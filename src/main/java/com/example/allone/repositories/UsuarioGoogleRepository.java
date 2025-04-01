package com.example.allone.repositories;

import com.example.allone.models.Usuario;
import com.example.allone.models.UsuarioGoogle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioGoogleRepository extends JpaRepository<UsuarioGoogle, Long> {
    Optional<UsuarioGoogle> findByUsername(String username);
    Page<UsuarioGoogle> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String nombre, String email, Pageable pageable);
}
